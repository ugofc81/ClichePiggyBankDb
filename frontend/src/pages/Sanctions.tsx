import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { usePiggyBank } from '../hooks/usePiggyBank';
import { SanctionValues, type SanctionAmount } from '../types';

export default function Sanctions() {
    // At the top of Sanctions():
    const {
        sanctions,
        users,
        loading,
        error,
        inquirerId,
        createSanction,
        likeSanction,
        unlikeSanction,
        page,
        setPage,
        totalPages,
        sort,
        setSort,
        order,
        setOrder
    } = usePiggyBank();

    // Local Form States
    const [receiverGuid, setReceiverGuid] = useState('');
    const [reason, setReason] = useState('');
    const [amount, setAmount] = useState<SanctionAmount>('STANDARD');
    const [formError, setFormError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Create string wrapper to persist state if they decide to navigate backward
    const querySuffix = inquirerId ? `?inquirerid=${inquirerId}` : '';

    // If not logged in cannot read
    if (!inquirerId) {
        return (
            <div style={{ padding: '40px 20px', fontFamily: 'sans-serif', textAlign: 'center' }}>
                <h2>Access Denied</h2>
                <p style={{ color: '#666', marginBottom: '20px' }}>
                    Please select a user profile to view or create sanctions.
                </p>
                <Link
                    to="/users"
                    style={{
                        display: 'inline-block',
                        padding: '10px 20px',
                        backgroundColor: '#0066cc',
                        color: 'white',
                        textDecoration: 'none',
                        borderRadius: '4px',
                        fontWeight: 'bold'
                    }}
                >
                    Login to be a user
                </Link>
            </div>
        );
    }

    // exclude yourself from sanctions
    const eligibleReceivers = users.filter(user => user.id !== inquirerId);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!receiverGuid || !reason) {
            setFormError('Please complete all form fields.');
            return;
        }

        try {
            setIsSubmitting(true);
            setFormError(null);

            if (createSanction) {
                await createSanction({ receiverGuid, reason, amount });
                // Clear form values on success
                setReason('');
                setReceiverGuid('');
                setAmount('STANDARD');
                alert('Sanction issued successfully!');
            }
        } catch (err: any) {
            setFormError(err.message || 'Submission failed.');
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
            <Link to={`/${querySuffix}`} style={{ color: '#0066cc' }}>Back to home</Link>

            <div style={{ display: 'flex', gap: '40px', marginTop: '20px' }}>
                {/* --- HERE FINISHES THE GENERAL CONTAINER --- */}

                {/* --- LEFT PANEL: CREATE SANCTION FORM --- */}
                <div style={{ flex: 1, borderRight: '1px solid #eee', paddingRight: '40px' }}>
                    <h3>Issue a New Sanction</h3>
                    <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>

                        <div>
                            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '5px' }}>Receiver:</label>
                            <select
                                value={receiverGuid}
                                onChange={(e) => setReceiverGuid(e.target.value)}
                                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
                            >
                                <option value="">-- Select Member --</option>
                                {eligibleReceivers.map(u => (
                                    <option key={u.id} value={u.id}>{u.name}</option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '5px' }}>Penalty Severity Tier:</label>
                            <select
                                value={amount}
                                onChange={(e) => setAmount(e.target.value as SanctionAmount)}
                                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #ccc' }}
                            >
                                <option value="LOW">LOW ({SanctionValues.LOW} units)</option>
                                <option value="STANDARD">STANDARD ({SanctionValues.STANDARD} units)</option>
                                <option value="HIGH">HIGH ({SanctionValues.HIGH} units)</option>
                            </select>
                        </div>

                        <div>
                            <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '5px' }}>Reason / Cliche Used:</label>
                            <textarea
                                value={reason}
                                onChange={(e) => setReason(e.target.value)}
                                placeholder="Why are they being fined?"
                                rows={4}
                                style={{ width: '100%', padding: '10px', borderRadius: '4px', border: '1px solid #ccc', resize: 'vertical' }}
                            />
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
                            {isSubmitting ? 'Submitting...' : 'File Sanction'}
                        </button>
                    </form>
                </div>
                {/* --- HERE FINISHES THE SANCTION CREATION FORM --- */}

                {/* --- RIGHT PANEL: SANCTIONS LIST --- */}
                <div style={{ flex: 2 }}>
                    {/* Header + Sort & Order Controls */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                        <h3 style={{ margin: 0 }}>Activity Feed</h3>

                        <div style={{ display: 'flex', gap: '8px' }}>
                            <select
                                value={sort}
                                onChange={(e) => setSort(e.target.value as 'datetime' | 'likes')}
                                style={{ padding: '6px 10px', borderRadius: '4px', border: '1px solid #ccc' }}
                            >
                                <option value="datetime">Sort by Date</option>
                                <option value="likes">Sort by Likes</option>
                            </select>

                            <button
                                type="button"
                                onClick={() => setOrder(order === 'desc' ? 'asc' : 'desc')}
                                style={{ padding: '6px 12px', borderRadius: '4px', border: '1px solid #ccc', background: '#fff', cursor: 'pointer' }}
                            >
                                {order === 'desc' ? '↓ Descending' : '↑ Ascending'}
                            </button>
                        </div>
                    </div>

                    {loading && <p>Syncing secure ledger data...</p>}
                    {error && <p style={{ color: 'red' }}>{error}</p>}

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {sanctions.map(s => (
                            <div
                                key={s.id}
                                style={{
                                    border: '1px solid #eaeaea',
                                    padding: '15px',
                                    borderRadius: '6px',
                                    backgroundColor: '#fafafa',
                                    position: 'relative'
                                }}
                            >
                                <p>
                                    <strong>{users.find(u => u.id === s.reporterGuid)?.name || 'A deleted reporter'}</strong> reported{' '}
                                    <strong>{users.find(u => u.id === s.receiverGuid)?.name || 'a deleted receiver.'}</strong>
                                </p>
                                <p style={{ color: '#555', margin: '5px 0' }}><em>"{s.reason}"</em></p>

                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '12px' }}>
                    <span style={{ fontSize: '12px', color: '#888' }}>
                        {s.amount} • {new Date(s.datetime).toLocaleString()}
                    </span>

                                    {/* Red Heart Toggle Button */}
                                    <button
                                        type="button"
                                        onClick={() => (s.liked ? unlikeSanction(s.id) : likeSanction(s.id))}
                                        style={{
                                            display: 'inline-flex',
                                            alignItems: 'center',
                                            gap: '6px',
                                            padding: '6px 12px',
                                            borderRadius: '20px',
                                            border: s.liked ? '1px solid #e0245e' : '1px solid #ccc',
                                            backgroundColor: s.liked ? '#ffeef2' : '#fff',
                                            color: s.liked ? '#e0245e' : '#555',
                                            fontWeight: 'bold',
                                            cursor: 'pointer'
                                        }}
                                    >
                                        <span>{s.liked ? '❤️' : '🤍'}</span>
                                        <span>{s.likesNumber}</span>
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Pagination Controls */}
                    {totalPages > 1 && (
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginTop: '20px' }}>
                            <button
                                type="button"
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                                style={{ padding: '6px 12px', borderRadius: '4px', border: '1px solid #ccc', cursor: page === 0 ? 'not-allowed' : 'pointer' }}
                            >
                                &laquo; Prev
                            </button>

                            {Array.from({ length: totalPages }, (_, idx) => (
                                <button
                                    key={idx}
                                    type="button"
                                    onClick={() => setPage(idx)}
                                    style={{
                                        padding: '6px 12px',
                                        borderRadius: '4px',
                                        border: '1px solid #ccc',
                                        backgroundColor: page === idx ? '#0066cc' : '#fff',
                                        color: page === idx ? '#fff' : '#333',
                                        fontWeight: page === idx ? 'bold' : 'normal',
                                        cursor: 'pointer'
                                    }}
                                >
                                    {idx + 1}
                                </button>
                            ))}

                            <button
                                type="button"
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page >= totalPages - 1}
                                style={{ padding: '6px 12px', borderRadius: '4px', border: '1px solid #ccc', cursor: page >= totalPages - 1 ? 'not-allowed' : 'pointer' }}
                            >
                                Next &raquo;
                            </button>
                        </div>
                    )}
                </div>
                {/* --- HERE FINISHES THE SANCTION LIST --- */}
                {/* --- HERE BEGINS THE GENERAL CONTAINER AGAIN --- */}

            </div>
        </div>
    );
}