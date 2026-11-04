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

    // 1. Check the liking status on mount or when the profile changes
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
            .then((response) => {
                // Reads response.data.isLiked correctly
                setHasLiked(response.data.isLiked);
            })
            .catch((err) => {
                console.error("Error evaluating like eligibility:", err);
            })
            .finally(() => {
                setChecking(false);
            });
    }, [sanctionId, inquirerId]);

    // 2. Handle the POST click operation
    const handleLikeClick = async () => {
        if (!inquirerId) return;

        try {
            // Targets your POST endpoint: POST /sanctions/{id}/like?inquirerid={uuid}
            await api.post(`/sanctions/${sanctionId}/like`, null, {
                params: { inquirerid: inquirerId }
            });

            // Toggle the local state instantly for snappy UI
            setHasLiked(!hasLiked);

            // Notify parent to fetch new counts / logs globally
            onLikeTriggered();
        } catch (err) {
            console.error("Failed to click like:", err);
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