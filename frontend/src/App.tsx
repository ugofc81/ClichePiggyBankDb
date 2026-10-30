import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Users from './pages/Users';
import Sanctions from './pages/Sanctions';
import Accounts from './pages/Accounts';

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/users" element={<Users />} />
                <Route path="/sanctions" element={<Sanctions />} />
                <Route path="/accounts" element={<Accounts />} />
            </Routes>
        </BrowserRouter>
    );
}