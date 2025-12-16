export default function Dashboard() {
    return (
        <div>
            <header className="mb-6">
                <h1>Dashboard</h1>
                <p className="text-secondary">Platform overview and key metrics</p>
            </header>

            <div className="grid grid-cols-4 gap-6 mb-6">
                <MetricCard title="Active Customers" value="12,847" change="+5.2%" />
                <MetricCard title="Open Alerts" value="156" change="-12.3%" positive />
                <MetricCard title="Cases in Progress" value="42" change="+8.1%" />
                <MetricCard title="Avg Risk Score" value="0.32" change="-2.4%" positive />
            </div>

            <div className="grid grid-cols-2 gap-6">
                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">Recent Alerts</h3>
                    </div>
                    <p className="text-muted">Alert data will be displayed here.</p>
                </div>

                <div className="card">
                    <div className="card-header">
                        <h3 className="card-title">Risk Distribution</h3>
                    </div>
                    <p className="text-muted">Risk distribution chart will be displayed here.</p>
                </div>
            </div>
        </div>
    );
}

interface MetricCardProps {
    title: string;
    value: string;
    change: string;
    positive?: boolean;
}

function MetricCard({ title, value, change, positive = false }: MetricCardProps) {
    const changeClass = change.startsWith('+')
        ? (positive ? 'badge-success' : 'badge-warning')
        : (positive ? 'badge-success' : 'badge-error');

    return (
        <div className="card">
            <p className="text-secondary text-sm mb-2">{title}</p>
            <p className="text-2xl font-bold mb-2">{value}</p>
            <span className={`badge ${changeClass}`}>{change}</span>
        </div>
    );
}
