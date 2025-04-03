import React from 'react';
import ReactDOM from 'react-dom/client';
import { HashRouter, Routes, Route } from 'react-router-dom';
import Home from "./pages/home/home";
import About from "./pages/about/about";
import NavBar from './components/navbar/navbar';
import Highlights from './pages/highlights/highlights';
import Adoption from './pages/adoption/adoption';
import Other from './pages/other/other';
import './App.css';
import './index.css';

function App() {

  return (
    <HashRouter>
      <NavBar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="highlights" element={<Highlights />} />
        <Route path="adoption" element={<Adoption />} />
        <Route path="other" element={<Other />} />
        <Route path="about" element={<About />} />
      </Routes>
    </HashRouter>
  );
}

export default App;