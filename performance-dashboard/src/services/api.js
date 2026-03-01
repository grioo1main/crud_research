
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const runLoadTest = async (config) => {
  const response = await fetch(`${API_URL}/api/loadtest/run`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(config)
  });
  return response.json();
};

export const getMetricsSummary = async (lastMinutes = 60) => {
  const response = await fetch(`${API_URL}/api/metrics/summary?lastMinutes=${lastMinutes}`);
  return response.json();
};

export const getMetricsHistory = async (lastMinutes = 60, bucketSeconds = 10) => {
  const response = await fetch(
    `${API_URL}/api/metrics/history?lastMinutes=${lastMinutes}&bucketSeconds=${bucketSeconds}`
  );
  return response.json();
};

export const clearAllMetrics = async () => {
  const response = await fetch(`${API_URL}/api/metrics/clear`, {
    method: 'DELETE'
  });
  // Возвращаем текст, не JSON
  const text = await response.text();
  return { success: response.ok, message: text };
};

export const clearLoadTestMetrics = async () => {
  const response = await fetch(`${API_URL}/api/metrics/clear/loadtest`, {
    method: 'DELETE'
  });
  // Возвращаем текст, не JSON
  const text = await response.text();
  return { success: response.ok, message: text };
};
