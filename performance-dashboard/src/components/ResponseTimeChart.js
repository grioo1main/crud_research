import React from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const METHOD_COLORS = {
  GET: { border: '#22c55e', bg: 'rgba(34,197,94,0.1)' },
  POST: { border: '#3b82f6', bg: 'rgba(59,130,246,0.1)' },
  PUT: { border: '#f97316', bg: 'rgba(249,115,22,0.1)' },
  DELETE: { border: '#ef4444', bg: 'rgba(239,68,68,0.1)' },
};

const ResponseTimeChart = ({ history }) => {
  if (!history || history.length === 0) {
    return <p className="no-data">Нет данных для отображения</p>;
  }

  const methods = [...new Set(history.map((h) => h.httpMethod))];
  const timestamps = [...new Set(history.map((h) => h.timestamp))].sort();

  const datasets = methods.map((method) => {
    const methodData = history.filter((h) => h.httpMethod === method);
    const dataMap = {};
    methodData.forEach((d) => {
      dataMap[d.timestamp] = d.avgResponseTimeMs;
    });

    return {
      label: method,
      data: timestamps.map((t) => dataMap[t] || null),
      borderColor: METHOD_COLORS[method]?.border || '#888',
      backgroundColor: METHOD_COLORS[method]?.bg || 'rgba(136,136,136,0.1)',
      fill: true,
      tension: 0.3,
      spanGaps: true,
    };
  });

  const labels = timestamps.map((t) => {
    const date = new Date(t);
    return date.toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  });

  const data = { labels, datasets };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'top' },
      tooltip: {
        callbacks: {
          label: (ctx) => `${ctx.dataset.label}: ${ctx.parsed.y} мс`,
        },
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        title: { display: true, text: 'Время отклика (мс)' },
      },
      x: {
        title: { display: true, text: 'Время' },
      },
    },
  };

  return (
    <div style={{ height: '350px' }}>
      <Line data={data} options={options} />
    </div>
  );
};

export default ResponseTimeChart;
