export default function Cases() {
    return (
        <div>
            <header className="mb-6">
                <h1>Cases</h1>
                <p className="text-secondary">Investigation case management</p>
            </header>

            <div className="card">
                <div className="card-header">
                    <h3 className="card-title">Active Cases</h3>
                    <button className="btn btn-primary">Create Case</button>
                </div>

                <table className="table">
                    <thead>
                        <tr>
                            <th>Case Number</th>
                            <th>Type</th>
                            <th>Priority</th>
                            <th>Status</th>
                            <th>Assigned To</th>
                            <th>Updated</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td className="font-mono">CASE-2025-0042</td>
                            <td>Suspicious Activity</td>
                            <td><span className="badge risk-high">High</span></td>
                            <td><span className="badge badge-warning">In Progress</span></td>
                            <td>[Analyst]</td>
                            <td>2025-01-16 09:15</td>
                            <td><button className="btn btn-secondary">Open</button></td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    );
}
