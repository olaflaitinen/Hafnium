import { Link } from 'react-router-dom';

export default function NotFound() {
    return (
        <div className="flex flex-col items-center justify-center" style={{ minHeight: '60vh' }}>
            <h1 className="text-3xl mb-4">404</h1>
            <p className="text-secondary mb-6">Page not found</p>
            <Link to="/" className="btn btn-primary">
                Return to Dashboard
            </Link>
        </div>
    );
}
