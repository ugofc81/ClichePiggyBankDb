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
    reporterGuid: string;
    receiverGuid: string;
    amount: SanctionAmount;
    reason: string;
    datetime: string; // ISO Date String
    likesNumber: number;
    liked: boolean;
}

export interface SpringPage<T> {
    content: T[];          // This is where your actual list array resides now
    totalElements: number; // Total rows in the database
    totalPages: number;    // Total pages based on size
    size: number;          // Items per page
    number: number;        // Current page index (0-indexed)
    first: boolean;
    last: boolean;
    empty: boolean;
}

export interface Account {
    id: string;
    ownerGuid: string;
    balance: number;
}