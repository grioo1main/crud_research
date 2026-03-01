import React, { useState } from 'react';
import { runLoadTest } from '../services/api';

const LoadTestPanel = ({ onTestComplete }) => {
  const [totalRequests, setTotalRequests] = useState(100);
  const [threads, setThreads] = useState(10);
  const [methods, setMethods] = useState(['GET', 'POST', 'PUT', 'DELETE']);
  const [loading, setLoading] = useState(false);
  const [progress, setProgress] = useState('');

  const toggleMethod = (method) => {
    setMethods((prev) =>
      prev.includes(method) ? prev.filter((m) => m !== method) : [...prev, method]
    );
  };

  const handleRun = async () => {
    if (methods.length === 0) {
      alert('Выберите хотя бы один HTTP-метод');
      return;
    }

    setLoading(true);
    setProgress('Запуск нагрузочного теста...');

    try {
      const res = await runLoadTest({ totalRequests, threads, methods });
      setProgress('Тест завершён!');
      onTestComplete(res.data);
    } catch (err) {
      setProgress('Ошибка: ' + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="load-test-panel">
      <h2>🚀 Нагрузочное тестирование</h2>
      <div className="test-controls">
        <div className="control-group">
          <label>Количество запросов:</label>
          <input
            type="number"
            value={totalRequests}
            onChange={(e) => setTotalRequests(parseInt(e.target.value) || 10)}
            min="10"
            max="10000"
            disabled={loading}
          />
        </div>

        <div className="control-group">
          <label>Потоков:</label>
          <input
            type="number"
            value={threads}
            onChange={(e) => setThreads(parseInt(e.target.value) || 1)}
            min="1"
            max="100"
            disabled={loading}
          />
        </div>

        <div className="control-group">
          <label>HTTP-методы:</label>
          <div className="method-toggles">
            {['GET', 'POST', 'PUT', 'DELETE'].map((m) => (
              <button
                key={m}
                className={`method-toggle ${methods.includes(m) ? 'active' : ''} ${m.toLowerCase()}`}
                onClick={() => toggleMethod(m)}
                disabled={loading}
              >
                {m}
              </button>
            ))}
          </div>
        </div>

        <button
          className="btn btn-primary run-btn"
          onClick={handleRun}
          disabled={loading}
        >
          {loading ? '⏳ Выполняется...' : '▶️ Запустить тест'}
        </button>
      </div>

      {progress && <div className="progress-text">{progress}</div>}
    </div>
  );
};

export default LoadTestPanel;
