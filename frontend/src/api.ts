import axios from 'axios';

// Create an instance configured for your Spring Boot backend
const api = axios.create({
    baseURL: 'http://localhost:9092/api', // Update this to match your Spring Boot context path
    headers: {
        'Content-Type': 'application/json',
    },
});

export default api;