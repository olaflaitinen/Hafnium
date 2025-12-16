export default function Alerts() {
    return (
        <div>
            <header className="mb-6">
                <h1>Alerts</h1>
                <p className="text-secondary">Transaction monitoring alerts</p>
            </header>

            <div className="card">
                <div className="card-header">
                    <h3 className="card-title">Alert Queue</h3>
                    <div className="flex gap-2">
                        <button className="btn btn-secondary">Filter</button>
                        <button className="btn btn-primary">Export</button>
                    </div>
                </div>

                <table className="table">
                    <thead>
                        <tr>
                            <th>Alert ID</th>
                            <th>Rule</th>
                            <th>Severity</th>
                            <th>Score</th>
                            <th>Status</th>
                            <th>Created</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td className="font-mono">ALT-0001</td>
                            <td>Unusual Transaction Amount</td>
                            <td><span className="badge risk-high">High</span></td>
                            <td>0.78</td>
                            <td><span className="badge badge-info">New</span></td>
                            <td>2025-01-16 10:30</td>
                            <td><button className="btn btn-secondary">Review</button></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
}
