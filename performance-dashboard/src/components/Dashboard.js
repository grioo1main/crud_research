
import React, { useState, useEffect, useCallback } from 'react';
import { getMetricsSummary, getMetricsHistory, clearAllMetrics, clearLoadTestMetrics } from '../services/api';
import LoadTestPanel from './LoadTestPanel';
import ResponseTimeChart from './ResponseTimeChart';
import ThroughputChart from './ThroughputChart';
import MethodComparison from './MethodComparison';
import MetricsTable from './MetricsTable';

const Dashboard = () => {
  const [summary, setSummary] = useState(null);
  const [history, setHistory] = useState([]);
  const [lastTestResult, setLastTestResult] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(true);

  const fetchData = useCallback(async () => {
    try {
      const [sumRes, histRes] = await Promise.all([
        getMetricsSummary(60),
        getMetricsHistory(60, 10),
      ]);
      setSummary(sumRes);
      setHistory(histRes);
    } catch (err) {
      console.error('Failed to fetch metrics:', err);
    }
  }, []);

  useEffect(() => {
    fetchData();
    if (!autoRefresh) return;
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, [fetchData, autoRefresh]);

  const handleTestComplete = (result) => {
    setLastTestResult(result);
    fetchData();
  };

  // Функции очистки - исправленные
  const handleClearAll = async () => {
    if (!window.confirm('Удалить ВСЕ метрики? Это действие необратимо!')) return;
    try {
      const result = await clearAllMetrics();
      if (result.success) {
        setSummary(null);
        setHistory([]);
        setLastTestResult(null);
        alert('✅ ' + result.message);
      } else {
        alert('❌ Ошибка: ' + result.message);
      }
    } catch (err) {
      alert('❌ Ошибка при удалении: ' + err.message);
    }
  };

  const handleClearLoadTests = async () => {
    if (!window.confirm('Удалить метрики нагрузочных тестов?')) return;
    try {
      const result = await clearLoadTestMetrics();
      if (result.success) {
        setLastTestResult(null);
        fetchData();
        alert('✅ ' + result.message);
      } else {
        alert('❌ Ошибка: ' + result.message);
      }
    } catch (err) {
      alert('❌ Ошибка при удалении: ' + err.message);
    }
  };

  return (
    <div className="dashboard">
      {/* Summary Cards */}
      <div className="summary-cards">
        <div className="card">
          <h3>Всего запросов</h3>
          <span className="card-value">{summary?.totalRequests || 0}</span>
        </div>
        <div className="card">
          <h3>Среднее время (мс)</h3>
          <span className="card-value">{summary?.avgResponseTimeMs?.toFixed(2) || 0}</span>
        </div>
        <div className="card">
          <h3>95-й перцентиль (мс)</h3>
          <span className="card-value">{summary?.p95ResponseTimeMs?.toFixed(2) || 0}</span>
        </div>
        <div className="card">
          <h3>RPS</h3>
          <span className="card-value">{summary?.requestsPerSecond?.toFixed(2) || 0}</span>
        </div>
        <div className="card">
          <h3>Ошибки</h3>
          <span className="card-value error">{summary?.errorRate?.toFixed(2) || 0}%</span>
        </div>
      </div>

      {/* Auto refresh toggle + кнопки очистки */}
      <div className="controls">
        <label>
          <input
            type="checkbox"
            checked={autoRefresh}
            onChange={(e) => setAutoRefresh(e.target.checked)}
          />
          Автообновление (5 сек)
        </label>
        <button onClick={fetchData} className="btn btn-secondary">
          🔄 Обновить
        </button>
        <button onClick={handleClearLoadTests} className="btn btn-warning">
          🧹 Очистить тесты
        </button>
        <button onClick={handleClearAll} className="btn btn-danger">
          ⚠️ Удалить всё
        </button>
      </div>

      {/* Load Test Panel */}
      <LoadTestPanel onTestComplete={handleTestComplete} />

      {/* Last Test Result */}
      {lastTestResult && (
        <div className="test-result">
          <h2>📊 Результаты последнего теста</h2>
          <div className="test-result-cards">
            <div className="card small">
              <h4>Запросов</h4>
              <span>{lastTestResult.totalRequests}</span>
            </div>
            <div className="card small">
              <h4>Успешных</h4>
              <span className="success">{lastTestResult.successfulRequests}</span>
            </div>
            <div className="card small">
              <h4>Ошибок</h4>
              <span className="error">{lastTestResult.failedRequests}</span>
            </div>
            <div className="card small">
              <h4>Общее время</h4>
              <span>{lastTestResult.totalTimeMs} мс</span>
            </div>
            <div className="card small">
              <h4>Среднее время</h4>
              <span>{lastTestResult.avgResponseTimeMs?.toFixed(2)} мс</span>
            </div>
            <div className="card small">
              <h4>RPS</h4>
              <span>{lastTestResult.requestsPerSecond?.toFixed(2)}</span>
            </div>
          </div>

          {/* Method stats table */}
          {lastTestResult.methodStats && (
            <table className="metrics-table">
              <thead>
                <tr>
                  <th>Метод</th>
                  <th>Кол-во</th>
                  <th>Среднее (мс)</th>
                  <th>Медиана (мс)</th>
                  <th>P95 (мс)</th>
                  <th>Мин (мс)</th>
                  <th>Макс (мс)</th>
                  <th>Ошибки</th>
                </tr>
              </thead>
              <tbody>
                {Object.entries(lastTestResult.methodStats).map(([method, stats]) => (
                  <tr key={method}>
                    <td><span className={`method-badge ${method.toLowerCase()}`}>{method}</span></td>
                    <td>{stats.count}</td>
                    <td>{stats.avgTimeMs?.toFixed(2)}</td>
                    <td>{stats.medianTimeMs?.toFixed(2)}</td>
                    <td>{stats.p95TimeMs?.toFixed(2)}</td>
                    <td>{stats.minTimeMs}</td>
                    <td>{stats.maxTimeMs}</td>
                    <td className={stats.errors > 0 ? 'error' : ''}>{stats.errors}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Charts */}
      <div className="charts-grid">
        <div className="chart-container">
          <h2>📈 Время отклика</h2>
          <ResponseTimeChart history={history} />
        </div>
        <div className="chart-container">
          <h2>📊 Пропускная способность по методам</h2>
          <ThroughputChart summary={summary} />
        </div>
      </div>

      <div className="chart-container full-width">
        <h2>🎯 Сравнение HTTP-методов</h2>
        <MethodComparison summary={summary} />
      </div>

      {/* Metrics breakdown table */}
      <div className="chart-container full-width">
        <h2>📋 Детализация по методам</h2>
        <MetricsTable summary={summary} />
      </div>
    </div>
  );
};

export default Dashboard;
