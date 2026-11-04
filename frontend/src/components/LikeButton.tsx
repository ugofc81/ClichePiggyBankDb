import { useEffect, useState } from 'react';
import api from '../api';
import type {Like} from "../types.ts";

interface LikeButtonProps {
    sanctionId: string;
    inquirerId: string | null;
    initialLikes: number;
    onLikeTriggered: () => void; // Refreshes the parent ledger feed
}

export default function LikeButton({ sanctionId, inquirerId, initialLikes, onLikeTriggered }: LikeButtonProps) {
    const [hasLiked, setHasLiked] = useState<boolean>(false);
    const [checking, setChecking] = useState<boolean>(true);

    useEffect(() => {
        if (!inquirerId) {
            setChecking(false);
            return;
        }

        setChecking(true);

        // GET /sanctions/{id}/like?inquirerid={uuid}
        api.get<Like>(`/sanctions/${sanctionId}/like`, {
            params: { inquirerid: inquirerId }
        })
            .then(() => {
                // 200 OK -> Resource exists, user liked it
                setHasLiked(true);
            })
            .catch((err) => {
                if (err.response && err.response.status === 404) {
                    // 404 Not Found -> Resource doesn't exist, user hasn't liked it
                    setHasLiked(false);
                } else {
                    console.error("Actual network or code error occurred", err);
                }
            })
            .finally(() => {
                setChecking(false);
            });
    }, [sanctionId, inquirerId]);

    const handleLikeClick = async () => {
        if (!inquirerId) return;

        try {
            // POST /sanctions/{id}/like?inquirerid={uuid}
            await api.post(`/sanctions/${sanctionId}/like`, null, {
                params: { inquirerid: inquirerId }
            });

            setHasLiked(!hasLiked); // Instantly toggle state
            onLikeTriggered();       // Refresh parent activity feeds
        } catch (err) {
            console.error("Failed to commit like:", err);
        }
    };

    if (checking) return <span style={{ fontSize: '14px', color: '#999' }}>...</span>;

    return (
        <button
            onClick={handleLikeClick}
            disabled={!inquirerId}
            style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '6px 12px',
                backgroundColor: hasLiked ? '#ffe6e6' : '#fff',
                border: hasLiked ? '1px solid #ff4d4d' : '1px solid #ccc',
                color: hasLiked ? '#ff4d4d' : '#333',
                borderRadius: '20px',
                cursor: inquirerId ? 'pointer' : 'not-allowed',
                fontWeight: 'bold',
                transition: 'all 0.2s ease'
            }}
        >
            {hasLiked ? '❤️' : '🤍'} {initialLikes}
        </button>
    );
}