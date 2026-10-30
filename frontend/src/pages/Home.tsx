import { Link } from 'react-router-dom';
import { usePiggyBank } from '../hooks/usePiggyBank';

export default function Home() {
    const { inquirerId, currentUser } = usePiggyBank();

    // Create string wrapper to persist state across route jumps
    const querySuffix = inquirerId ? `?inquirerid=${inquirerId}` : '';

    return (
        <div style={{ maxWidth: '600px', margin: '8px auto', padding: '40px 20px', fontFamily: 'sans-serif', textAlign: 'center' }}>
            <h1>Cliche Piggy Bank</h1>

            {currentUser && (
                <div style={{ backgroundColor: '#e2f0d9', padding: '10px', borderRadius: '6px', marginBottom: '20px', fontWeight: 'bold', color: '#385723' }}>
                    Acting as: {currentUser.name}
                </div>
            )}

            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', marginTop: '20px' }}>
                <Link to={`/users${querySuffix}`} style={{ padding: '15px', backgroundColor: '#0066cc', color: 'white', textDecoration: 'none', borderRadius: '6px', fontWeight: 'bold' }}>View Users</Link>
                <Link to={`/sanctions${querySuffix}`} style={{ padding: '15px', backgroundColor: '#0066cc', color: 'white', textDecoration: 'none', borderRadius: '6px', fontWeight: 'bold' }}>View/Create Sanctions</Link>
                <Link to={`/accounts${querySuffix}`} style={{ padding: '15px', backgroundColor: '#0066cc', color: 'white', textDecoration: 'none', borderRadius: '6px', fontWeight: 'bold' }}>View Accounts</Link>
            </div>
        </div>
    );
}