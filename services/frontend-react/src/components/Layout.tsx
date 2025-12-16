import { Outlet, NavLink } from 'react-router-dom';

const navItems = [
    { path: '/', label: 'Dashboard', icon: 'dashboard' },
    { path: '/customers', label: 'Customers', icon: 'people' },
    { path: '/alerts', label: 'Alerts', icon: 'warning' },
    { path: '/cases', label: 'Cases', icon: 'folder' },
];

export default function Layout() {
    return (
        <div className="flex">
            <aside className="sidebar">
                <div className="mb-6">
                    <h1 className="text-xl font-bold">Hafnium</h1>
                    <p className="text-muted text-sm">Compliance Platform</p>
                </div>

                <nav className="flex flex-col gap-2">
                    {navItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) =>
                                `nav-link ${isActive ? 'nav-link-active' : ''}`
                            }
                        >
                            {item.label}
                        </NavLink>
                    ))}
                </nav>
            </aside>

            <main className="main-content">
                <Outlet />
            </main>
        </div>
    );
}
