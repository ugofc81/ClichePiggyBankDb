import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../api';
import type {User, Sanction, Account} from '../types';

export function usePiggyBank() {
    const [users, setUsers] = useState<User[]>([]);
    const [sanctions, setSanctions] = useState<Sanction[]>([]);
    const [accounts, setAccounts] = useState<Account[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Use React Router to watch query string parameters
    const [searchParams] = useSearchParams();
    const inquirerId = searchParams.get('inquirerid');

    const fetchData = async () => {
        try {
            setLoading(true);
            setError(null);

            // 1. Fetch Users independently (This should always work)
            try {
                const usersRes = await api.get<User[]>('/users');
                setUsers(usersRes.data);
            } catch (userErr) {
                console.error('Failed to load users:', userErr);
                setError('Could not load user directory.');
            }

            // 2. Fetch Sanctions only if inquirerId is active, otherwise reset array
            if (inquirerId) {
                try {
                    const sanctionsRes = await api.get<Sanction[]>('/sanctions', {
                        params: { inquirerid: inquirerId }
                    });
                    setSanctions(sanctionsRes.data);
                } catch (sanctionErr) {
                    console.error('Failed to load sanctions secure view:', sanctionErr);
                    // Don't crash the whole hook; just set an isolated view state error or clear ledger
                    setSanctions([]);
                }
            } else {
                setSanctions([]); // Clear if no one is logged in
            }

            // 3. Fetch Accounts independently (This should always work)
            try {
                const accountsRes = await api.get<Account[]>('/accounts');
                setAccounts(accountsRes.data);
            } catch (accountErr) {
                console.error('Failed to load accounts:', accountErr);
                setError('Could not load account directory.');
            }
        } catch (globalErr) {
            console.error(globalErr);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [inquirerId]); // Re-fetch data instantly if the logged-in user changes in the URL

    const createSanction = async (newSanction: { receiverId: string; reason: string; amount: string }) => {
        if (!inquirerId) return;

        // Build the payload matching what your Spring Boot Controller expects
        // If your backend maps using entire objects, we pass the IDs or look them up
        const payload = {
            reporter: { id: inquirerId },
            receiver: { id: newSanction.receiverId },
            amount: newSanction.amount, // Passes "LOW", "STANDARD", or "HIGH"
            reason: newSanction.reason,
        };

        try {
            await api.post('/sanctions', payload, {
                params: { inquirerid: inquirerId } // Append the query parameter if your security filter requires it
            });
            await fetchData(); // Refresh the list of sanctions instantly on success!
        } catch (err) {
            console.error('Failed to issue sanction:', err);
            throw new Error('Could not submit sanction to the server.');
        }
    };

    const createUser = async (newUser: { name: string; roles: string[]; }) => {
        if (!inquirerId) return;

        // Build the payload matching what your Spring Boot Controller expects
        // If your backend maps using entire objects, we pass the IDs or look them up
        const payload = {
            name: newUser.name,
            roles: newUser.roles,
        };

        try {
            await api.post('/users', payload, {
                params: { inquirerid: inquirerId } // Append the query parameter if your security filter requires it
            });
            await fetchData(); // Refresh the list of sanctions instantly on success!
        } catch (err) {
            console.error('Failed to create user:', err);
            throw new Error('Could not create user into the server.');
        }
    };

    const deleteUser = async (userId: string) => {
        if (!inquirerId) return;

        try {
            await api.delete(`/users/${userId}`, {
                params: { inquirerid: inquirerId } // Append the query parameter if your security filter requires it
            });
            await fetchData(); // Refresh the list of sanctions instantly on success!
        } catch (err) {
            console.error('Failed to delete user:', err);
            throw new Error('Could not delete user from the server.');
        }
    };
    const likeSanction = async (sanctionId: string) => {
        if (!inquirerId) return;

        try {
            await api.put(`/sanctions/${sanctionId}/like`, null, {
                params: { inquirerid: inquirerId }
            });

            // Refresh data instantly so the UI updates the like count and likedBy list
            await fetchData();
        } catch (err) {
            console.error('Failed to register like:', err);
            alert('Could not process like request.');
        }
    };

    const unlikeSanction = async (sanctionId: string) => {
        if (!inquirerId) return;

        try {
            await api.put(`/sanctions/${sanctionId}/unlike`, null, {
                params: { inquirerid: inquirerId }
            });

            // Refresh data instantly so the UI updates the like count and likedBy list
            await fetchData();
        } catch (err) {
            console.error('Failed to register unlike:', err);
            alert('Could not process unlike request.');
        }
    };

    const resetAccount = async (accountId: string) => {
        if (!inquirerId) return;

        try {
            await api.put(`/accounts/${accountId}/reset`, null, {
                params: { inquirerid: inquirerId }
            });

            // Refresh data instantly so the UI updates the like count and likedBy list
            await fetchData();
        } catch (err) {
            console.error('Failed to reset account:', err);
            alert('Could not process reset account.');
        }
    };

    // Find the full User object corresponding to the URL ID if it exists
    const currentUser = users.find(u => u.id === inquirerId) || null;

    return { users, sanctions, accounts, loading, error, currentUser, inquirerId, likeSanction, unlikeSanction, createSanction, resetAccount, createUser, deleteUser, refresh: fetchData };
}