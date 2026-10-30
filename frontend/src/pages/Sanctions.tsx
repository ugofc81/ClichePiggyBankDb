import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { usePiggyBank } from '../hooks/usePiggyBank';
import { SanctionValues, type SanctionAmount } from '../types';

export default function Sanctions() {
    const { sanctions, users, loading, error, inquirerId, createSanction, likeSanction, unlikeSanction } = usePiggyBank();

    // Local Form States
    const [receiverId, setReceiverId] = useState('');
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
        if (!receiverId || !reason) {
            setFormError('Please complete all form fields.');
            return;
        }

        try {
            setIsSubmitting(true);
            setFormError(null);

            if (createSanction) {
                await createSanction({ receiverId, reason, amount });
                // Clear form values on success
                setReason('');
                setReceiverId('');
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
                                value={receiverId}
                                onChange={(e) => setReceiverId(e.target.value)}
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
                    <h3>Activity Feed</h3>
                    {loading && <p>Syncing secure ledger data...</p>}
                    {error && <p style={{ color: 'red' }}>{error}</p>}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {sanctions.map(s => {
                            // Check if the currently logged-in user has already liked this sanction
                            const hasLiked = s.likedBy?.includes(inquirerId || '');

                            return (
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
                                    <p><strong>{s.reporter.name}</strong> reported <strong>{s.receiver.name}</strong></p>
                                    <p style={{ color: '#555', margin: '5px 0' }}><em>"{s.reason}"</em></p>

                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '12px' }}>
                                        <span style={{ backgroundColor: '#ffebe9', color: '#ff1a1a', padding: '3px 6px', borderRadius: '4px', fontWeight: 'bold', fontSize: '13px' }}>
                                            Amount: {s.amount} ({SanctionValues[s.amount]} €)
                                        </span>

                                        {/* --- INTERACTIVE LIKE BUTTON --- */}
                                        <button
                                            onClick={() => hasLiked ? unlikeSanction && unlikeSanction(s.id) : likeSanction && likeSanction(s.id)}
                                            style={{
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: '6px',
                                                padding: '6px 12px',
                                                backgroundColor: hasLiked ? '#ffe6e6' : '#fff',
                                                border: hasLiked ? '1px solid #ff4d4d' : '1px solid #ccc',
                                                color: hasLiked ? '#ff4d4d' : '#333',
                                                borderRadius: '20px',
                                                cursor: 'pointer',
                                                fontWeight: 'bold',
                                                transition: 'all 0.2s ease'
                                            }}
                                        >
                                            {hasLiked ? '❤️' : '🤍'} {s.likes || 0}
                                        </button>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
                {/* --- HERE FINISHES THE SANCTION LIST --- */}
                {/* --- HERE BEGINS THE GENERAL CONTAINER AGAIN --- */}

            </div>
        </div>
    );
}