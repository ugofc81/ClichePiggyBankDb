import axios from 'axios';

// Create an instance configured for your Spring Boot backend
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;