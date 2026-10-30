export interface User {
    id: string; // UUID
    name: string;
    roles: string[];
}

export type SanctionAmount = 'LOW' | 'STANDARD' | 'HIGH';

// Define a map that links the string name to its decimal value
export const SanctionValues: Record<SanctionAmount, number> = {
    LOW: 0.5,
    STANDARD: 1,
    HIGH: 2
};

export interface Sanction {
    id: string;
    reporter: User;
    receiver: User;
    amount: SanctionAmount;
    reason: string;
    datetime: string; // ISO Date String
    likedBy: string[]; // Set<UUID>
    likes: number;
}

export interface Account {
    id: string;
    ownerId: string;
    balance: number;
}
