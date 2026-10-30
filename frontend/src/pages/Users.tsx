import { Link, useSearchParams } from 'react-router-dom';
import { usePiggyBank } from '../hooks/usePiggyBank';
import React, {useState} from "react";

export default function Users() {
    const { users, loading, error, inquirerId, createUser, deleteUser } = usePiggyBank();

    // Local Form States
    const [name, setName] = useState('');
    const [roles, setRoles] = useState(['']);
    const [, setSearchParams] = useSearchParams();
    const [formError, setFormError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleLogin = (userId: string) => {
        // Sets ?inquirerid=userId into the browser URL string
        setSearchParams({ inquirerid: userId });
    };

    const handleLogout = () => {
        setSearchParams({}); // Clears all query parameters
    };

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!name || !roles) {
            setFormError('Please complete all form fields.');
            return;
        }

        try {
            setIsSubmitting(true);
            setFormError(null);

            if (createUser) {
                await createUser({ name, roles });
                // Clear form values on success
                setName('');
                setRoles(['']);
                alert('User created successfully!');
            }
        } catch (err: any) {
            setFormError(err.message || 'Submission failed.');
        } finally {
            setIsSubmitting(false);
        }
    };

    // Helper string to attach to navigation links so pages don't lose the query string context
    const querySuffix = inquirerId ? `?inquirerid=${inquirerId}` : '';

    return (
        <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
            <Link to={`/${querySuffix}`} style={{ color: '#0066cc' }}>Back to home</Link>
            <div style={{ display: 'flex', gap: '40px', marginTop: '20px' }}>
                {/* --- HERE FINISHES THE GENERAL CONTAINER --- */}


                {/* --- LEFT PANEL: CREATE USER FORM --- */}
                <div style={{ flex: 1, borderRight: '1px solid #eee', paddingRight: '40px' }}>
                    <h3>Create a New User</h3>
                    <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                        <div>
                            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '5px' }}>User name:</label>
                            <textarea
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="User name"
                                rows={1}
                                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
                            />
                        </div>

                        <div>
                            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '5px' }}>Role(s):</label>
                            <select
                                name="selectedRoles"
                                defaultValue={[]}
                                multiple={true}
                                onChange={e => {
                                    const options = [...e.target.selectedOptions];
                                    const values = options.map(option => option.value);
                                    setRoles(values);
                                }}
                            >
                                <option value="user">user</option>
                                <option value="admin">admin</option>
                            </select>
                        </div>

                        {formError && <p style={{ color: 'red', margin: 0 }}>{formError}</p>}
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            style={{
                                padding: '12px',
                                backgroundColor: isSubmitting ? '#aaa' : '#e60000',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                fontSize: '16px',
                                fontWeight: 'bold',
                                cursor: isSubmitting ? 'not-allowed' : 'pointer'
                            }}
                        >
                            {isSubmitting ? 'Creating...' : 'Create user'}
                        </button>
                    </form>
                </div>
                {/* --- HERE FINISHES THE SANCTION CREATION FORM --- */}

                {/* --- RIGHT PANEL: USERS LIST --- */}
                <div style={{ flex: 2 }}>
                <h2>Active Users</h2>

                {loading && <p>Loading users...</p>}
                {error && <p style={{ color: 'red' }}>{error}</p>}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '15px' }}>
                    {users.map(user => {
                        const isCurrent = user.id === inquirerId;
                        return (
                            <div
                                key={user.id}
                                style={{
                                    alignItems: 'center',
                                    backgroundColor: isCurrent ? '#f0f7ff' : '#fff',
                                    border: isCurrent ? '2px solid #0066cc' : '1px solid #ccc',
                                    borderRadius: '6px',
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    padding: '12px',
                                }}
                            >
                                <div>
                                    <strong>{user.name}</strong> <small style={{ color: 'gray' }}>({user.roles.join(', ')})</small>
                                    {isCurrent && <span style={{ marginLeft: '10px', color: '#0066cc', fontWeight: 'bold' }}>● Active Profile</span>}
                                </div>

                                {isCurrent ? (
                                    <button
                                        onClick={handleLogout}
                                        style={{ padding: '6px 12px', backgroundColor: '#fee', color: '#c00', border: '1px solid #c00', borderRadius: '4px', cursor: 'pointer' }}
                                    >
                                        Logout
                                    </button>
                                ) : (
                                    <button
                                        onClick={() => handleLogin(user.id)}
                                        style={{ padding: '6px 12px', backgroundColor: '#0066cc', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                                    >
                                        Login as {user.name}
                                    </button>
                                )}

                                {isCurrent ? (
                                    <button
                                        style={{ padding: '6px 12px', backgroundColor: '#bfbfbf', color: '#000000', border: '1px solid #000000', borderRadius: '4px', cursor: 'pointer' }}
                                    >
                                        Cannot delete yourself
                                    </button>
                                ) : (
                                    <button
                                        onClick={() => deleteUser && deleteUser(user.id)}
                                        style={{ padding: '6px 12px', backgroundColor: '#0066cc', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
                                    >
                                        Delete {user.name}
                                    </button>
                                )}
                            </div>
                        );
                    })}
                </div>
                </div>
            </div>
        </div>
    );
}
