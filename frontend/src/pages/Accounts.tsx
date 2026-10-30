import { Link } from 'react-router-dom';
import { usePiggyBank } from '../hooks/usePiggyBank';

export default function Accounts() {
    const { accounts, loading, error, inquirerId, resetAccount, users } = usePiggyBank();

    // Create string wrapper to persist state if they decide to navigate backward
    const querySuffix = inquirerId ? `?inquirerid=${inquirerId}` : '';

    return (
        <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
            <Link to={`/${querySuffix}`} style={{ color: '#0066cc' }}>← Back to Landing Page</Link>
            <h2>Active Accounts</h2>
            {loading && <p>Loading users...</p>}
            {error && <p style={{ color: 'red' }}>{error}</p>}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '15px' }}>
                {accounts.map(a => {
                    const isNonZero = a.balance > 0;
                    return (
                        <div
                            key={a.id}
                            style={{
                                alignItems: 'center',
                                backgroundColor: isNonZero ? '#fafafa' : '#fff',
                                border: isNonZero ? '2px solid #0066cc' : '1px solid #ccc',
                                borderRadius: '6px',
                                display: 'flex',
                                justifyContent: 'space-between',
                                padding: '15px',
                                position: 'relative',
                        }}
                        >
                            <p><strong>{users.find(u => u.id === a.ownerId)?.name}</strong> owes <strong>{a.balance}</strong></p>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '12px' }}>
                                {/* --- RESET BUTTON --- */}
                                <button
                                    onClick={() => resetAccount(a.id)}
                                    disabled={!isNonZero}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '6px',
                                        padding: '6px 12px',
                                        backgroundColor: isNonZero ? '#f0f7ff' : '#fff',
                                        border: isNonZero ? '2px solid #0066cc' : '1px solid #ccc',
                                        color: '#333',
                                        borderRadius: '20px',
                                        cursor: 'pointer',
                                        fontWeight: 'bold',
                                        transition: 'all 0.2s ease',
                                }}
                                >
                                    'Reset'
                                </button>
                            </div>
                        </div>
                    );
                })
                }
            </div>
        </div>
    );
}