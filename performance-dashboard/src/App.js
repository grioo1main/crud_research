import React from 'react';
import Dashboard from './components/Dashboard';
import './styles/App.css';

function App() {
  return (
    <div className="app">
      <header className="app-header">
        <h1>⚡ Server Performance Analyzer</h1>
        <p>Анализ производительности HTTP-методов и TCP-соединений</p>
      </header>
      <Dashboard />
    </div>
  );
}

export default App;
