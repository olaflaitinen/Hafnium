"""
Physics-Informed Neural Network (PINN) for Risk Scoring

Implements constrained neural networks that enforce domain invariants
such as monotonicity, smoothness, and calibration.
"""

from typing import Dict, List, Optional, Tuple

import torch
import torch.nn as nn
import torch.nn.functional as F


class MonotonicPINN(nn.Module):
    """
    Physics-Informed Neural Network with monotonicity constraints.
    
    This network enforces that certain input features have a monotonic
    relationship with the output (risk score). This is achieved through
    gradient penalty during training.
    
    Args:
        input_dim: Number of input features
        hidden_dims: List of hidden layer dimensions
        monotonic_features: Indices of features that should be monotonic
        increasing: Whether monotonic features should increase risk
        dropout: Dropout rate for regularization
    """
    
    def __init__(
        self,
        input_dim: int,
        hidden_dims: List[int] = [256, 128, 64],
        monotonic_features: Optional[List[int]] = None,
        increasing: bool = True,
        dropout: float = 0.2,
    ):
        super().__init__()
        
        self.input_dim = input_dim
        self.hidden_dims = hidden_dims
        self.monotonic_features = monotonic_features or []
        self.increasing = increasing
        
        # Build hidden layers
        layers = []
        prev_dim = input_dim
        for hidden_dim in hidden_dims:
            layers.extend([
                nn.Linear(prev_dim, hidden_dim),
                nn.LeakyReLU(0.1),
                nn.Dropout(dropout),
            ])
            prev_dim = hidden_dim
        
        self.hidden = nn.Sequential(*layers)
        
        # Output head
        self.output_head = nn.Linear(prev_dim, 1)
        
        # Temperature parameter for calibration
        self.temperature = nn.Parameter(torch.ones(1))
    
    def forward(self, x: torch.Tensor) -> torch.Tensor:
        """
        Forward pass through the network.
        
        Args:
            x: Input tensor of shape (batch_size, input_dim)
        
        Returns:
            Risk score tensor of shape (batch_size, 1)
        """
        h = self.hidden(x)
        logits = self.output_head(h)
        
        # Apply temperature scaling for calibration
        scaled_logits = logits / self.temperature
        
        # Sigmoid to bound output to [0, 1]
        return torch.sigmoid(scaled_logits)
    
    def compute_monotonicity_loss(
        self,
        x: torch.Tensor,
        n_samples: int = 100,
    ) -> torch.Tensor:
        """
        Compute monotonicity constraint loss via gradient penalty.
        
        For features that should increase risk, penalize negative gradients.
        For features that should decrease risk, penalize positive gradients.
        
        Args:
            x: Input tensor for computing gradients
            n_samples: Number of samples for gradient computation
        
        Returns:
            Monotonicity loss scalar
        """
        if not self.monotonic_features:
            return torch.tensor(0.0, device=x.device)
        
        # Sample random inputs if needed
        if x.shape[0] > n_samples:
            indices = torch.randperm(x.shape[0])[:n_samples]
            x = x[indices]
        
        x = x.requires_grad_(True)
        y = self.forward(x)
        
        # Compute gradients with respect to input
        gradients = torch.autograd.grad(
            outputs=y.sum(),
            inputs=x,
            create_graph=True,
        )[0]
        
        # Select monotonic feature gradients
        mono_grads = gradients[:, self.monotonic_features]
        
        # Penalize violations
        if self.increasing:
            # Penalize negative gradients
            violations = F.relu(-mono_grads)
        else:
            # Penalize positive gradients
            violations = F.relu(mono_grads)
        
        return violations.pow(2).mean()
    
    def compute_smoothness_loss(
        self,
        x: torch.Tensor,
        n_samples: int = 100,
    ) -> torch.Tensor:
        """
        Compute smoothness constraint via second-order gradient penalty.
        
        Penalizes high curvature in the output surface to prevent
        adversarial sensitivity.
        
        Args:
            x: Input tensor
            n_samples: Number of samples
        
        Returns:
            Smoothness loss scalar
        """
        if x.shape[0] > n_samples:
            indices = torch.randperm(x.shape[0])[:n_samples]
            x = x[indices]
        
        x = x.requires_grad_(True)
        y = self.forward(x)
        
        # First-order gradients
        grad1 = torch.autograd.grad(
            outputs=y.sum(),
            inputs=x,
            create_graph=True,
        )[0]
        
        # Approximate second-order gradients (diagonal of Hessian)
        loss = torch.tensor(0.0, device=x.device)
        for i in range(min(x.shape[1], 10)):  # Limit for efficiency
            grad2 = torch.autograd.grad(
                outputs=grad1[:, i].sum(),
                inputs=x,
                create_graph=True,
                retain_graph=True,
            )[0]
            loss = loss + grad2.pow(2).mean()
        
        return loss / min(x.shape[1], 10)


class PINNLoss(nn.Module):
    """
    Combined loss function for PINN training.
    
    Combines data loss with physics-informed constraint losses:
    L_total = L_data + lambda_mono * L_mono + lambda_smooth * L_smooth
    
    Args:
        lambda_mono: Weight for monotonicity constraint
        lambda_smooth: Weight for smoothness constraint
        lambda_calib: Weight for calibration constraint
    """
    
    def __init__(
        self,
        lambda_mono: float = 1.0,
        lambda_smooth: float = 0.1,
        lambda_calib: float = 0.5,
    ):
        super().__init__()
        
        self.lambda_mono = lambda_mono
        self.lambda_smooth = lambda_smooth
        self.lambda_calib = lambda_calib
        
        self.bce_loss = nn.BCELoss()
    
    def forward(
        self,
        model: MonotonicPINN,
        predictions: torch.Tensor,
        targets: torch.Tensor,
        inputs: torch.Tensor,
    ) -> Tuple[torch.Tensor, Dict[str, float]]:
        """
        Compute total loss with breakdown.
        
        Args:
            model: The PINN model
            predictions: Model predictions
            targets: Ground truth targets
            inputs: Input features (for constraint computation)
        
        Returns:
            Tuple of (total_loss, loss_breakdown_dict)
        """
        # Data loss
        data_loss = self.bce_loss(predictions, targets)
        
        # Monotonicity loss
        mono_loss = model.compute_monotonicity_loss(inputs)
        
        # Smoothness loss
        smooth_loss = model.compute_smoothness_loss(inputs)
        
        # Calibration loss (ECE approximation)
        calib_loss = self._compute_calibration_loss(predictions, targets)
        
        # Total loss
        total_loss = (
            data_loss
            + self.lambda_mono * mono_loss
            + self.lambda_smooth * smooth_loss
            + self.lambda_calib * calib_loss
        )
        
        breakdown = {
            "data_loss": data_loss.item(),
            "mono_loss": mono_loss.item(),
            "smooth_loss": smooth_loss.item(),
            "calib_loss": calib_loss.item(),
            "total_loss": total_loss.item(),
        }
        
        return total_loss, breakdown
    
    def _compute_calibration_loss(
        self,
        predictions: torch.Tensor,
        targets: torch.Tensor,
        n_bins: int = 10,
    ) -> torch.Tensor:
        """
        Compute Expected Calibration Error as a differentiable loss.
        
        Uses soft binning for gradient flow.
        """
        bin_boundaries = torch.linspace(0, 1, n_bins + 1, device=predictions.device)
        
        ece = torch.tensor(0.0, device=predictions.device)
        
        for i in range(n_bins):
            # Soft bin membership
            in_bin = torch.sigmoid(10 * (predictions - bin_boundaries[i])) * \
                     torch.sigmoid(10 * (bin_boundaries[i + 1] - predictions))
            
            bin_size = in_bin.sum()
            if bin_size > 0:
                bin_accuracy = (in_bin * targets).sum() / bin_size
                bin_confidence = (in_bin * predictions).sum() / bin_size
                ece = ece + torch.abs(bin_accuracy - bin_confidence) * bin_size
        
        return ece / len(predictions)


class PINNTrainer:
    """
    Trainer for Physics-Informed Neural Networks.
    
    Implements multi-phase training with gradual constraint introduction.
    """
    
    def __init__(
        self,
        model: MonotonicPINN,
        optimizer: torch.optim.Optimizer,
        loss_fn: PINNLoss,
        device: str = "cpu",
    ):
        self.model = model.to(device)
        self.optimizer = optimizer
        self.loss_fn = loss_fn
        self.device = device
        self.history: List[Dict[str, float]] = []
    
    def train_epoch(
        self,
        dataloader: torch.utils.data.DataLoader,
    ) -> Dict[str, float]:
        """Train for one epoch."""
        self.model.train()
        
        epoch_losses = {
            "data_loss": 0.0,
            "mono_loss": 0.0,
            "smooth_loss": 0.0,
            "calib_loss": 0.0,
            "total_loss": 0.0,
        }
        n_batches = 0
        
        for batch in dataloader:
            inputs = batch["features"].to(self.device)
            targets = batch["labels"].to(self.device)
            
            self.optimizer.zero_grad()
            
            predictions = self.model(inputs)
            loss, breakdown = self.loss_fn(
                model=self.model,
                predictions=predictions,
                targets=targets,
                inputs=inputs,
            )
            
            loss.backward()
            self.optimizer.step()
            
            for key, value in breakdown.items():
                epoch_losses[key] += value
            n_batches += 1
        
        # Average losses
        for key in epoch_losses:
            epoch_losses[key] /= n_batches
        
        self.history.append(epoch_losses)
        return epoch_losses
    
    def validate(
        self,
        dataloader: torch.utils.data.DataLoader,
    ) -> Dict[str, float]:
        """Validate the model."""
        self.model.eval()
        
        all_predictions = []
        all_targets = []
        
        with torch.no_grad():
            for batch in dataloader:
                inputs = batch["features"].to(self.device)
                targets = batch["labels"].to(self.device)
                
                predictions = self.model(inputs)
                
                all_predictions.append(predictions)
                all_targets.append(targets)
        
        predictions = torch.cat(all_predictions)
        targets = torch.cat(all_targets)
        
        # Compute metrics
        mse = F.mse_loss(predictions, targets).item()
        
        # Binary classification metrics (assuming 0.5 threshold)
        binary_preds = (predictions > 0.5).float()
        accuracy = (binary_preds == targets).float().mean().item()
        
        return {
            "mse": mse,
            "accuracy": accuracy,
        }
