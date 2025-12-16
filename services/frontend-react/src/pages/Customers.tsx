export default function Customers() {
    return (
        <div>
            <header className="mb-6">
                <h1>Customers</h1>
                <p className="text-secondary">Manage customer onboarding and KYC status</p>
            </header>

            <div className="card">
                <div className="card-header">
                    <h3 className="card-title">Customer List</h3>
                    <button className="btn btn-primary">Add Customer</button>
                </div>

                <table className="table">
                    <thead>
                        <tr>
                            <th>Customer ID</th>
                            <th>Name</th>
                            <th>Status</th>
                            <th>Risk Tier</th>
                            <th>Created</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td className="font-mono">CUST-001</td>
                            <td>[Customer Name]</td>
                            <td><span className="badge badge-success">Verified</span></td>
                            <td><span className="badge risk-low">Low</span></td>
                            <td>2025-01-15</td>
                            <td><button className="btn btn-secondary">View</button></td>
                        </tr>
                        <tr>
                            <td className="font-mono">CUST-002</td>
                            <td>[Customer Name]</td>
                            <td><span className="badge badge-warning">Pending</span></td>
                            <td><span className="badge risk-medium">Medium</span></td>
                            <td>2025-01-14</td>
                            <td><button className="btn btn-secondary">View</button></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
}
