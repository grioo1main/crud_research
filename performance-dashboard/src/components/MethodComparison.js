import React from 'react';
import { Radar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  RadialLinearScale,
  PointElement,
  LineElement,
  Filler,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

const MethodComparison = ({ summary }) => {
  if (!summary || !summary.byMethod || Object.keys(summary.byMethod).length === 0) {
    return <p className="no-data">Нет данных для отображения</p>;
  }

  const methods = Object.keys(summary.byMethod);
  const colors = {
    GET: { border: '#22c55e', bg: 'rgba(34,197,94,0.2)' },
    POST: { border: '#3b82f6', bg: 'rgba(59,130,246,0.2)' },
    PUT: { border: '#f97316', bg: 'rgba(249,115,22,0.2)' },
    DELETE: { border: '#ef4444', bg: 'rgba(239,68,68,0.2)' },
  };

  const labels = ['Среднее время', 'Медиана', 'P95', 'Макс время', 'Кол-во запросов'];

  // Normalize values for radar
  const maxVals = {
    avg: Math.max(...methods.map((m) => summary.byMethod[m].avgTimeMs), 1),
    median: Math.max(...methods.map((m) => summary.byMethod[m].medianTimeMs), 1),
    p95: Math.max(...methods.map((m) => summary.byMethod[m].p95TimeMs), 1),
    max: Math.max(...methods.map((m) => summary.byMethod[m].maxTimeMs), 1),
    count: Math.max(...methods.map((m) => summary.byMethod[m].count), 1),
  };

  const datasets = methods.map((method) => {
    const d = summary.byMethod[method];
    return {
      label: method,
      data: [
        (d.avgTimeMs / maxVals.avg) * 100,
        (d.medianTimeMs / maxVals.median) * 100,
        (d.p95TimeMs / maxVals.p95) * 100,
        (d.maxTimeMs / maxVals.max) * 100,
        (d.count / maxVals.count) * 100,
      ],
      borderColor: colors[method]?.border || '#888',
      backgroundColor: colors[method]?.bg || 'rgba(136,136,136,0.2)',
      borderWidth: 2,
    };
  });

  const data = { labels, datasets };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      r: {
        beginAtZero: true,
        max: 110,
        ticks: { display: false },
      },
    },
    plugins: {
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const method = ctx.dataset.label;
            const d = summary.byMethod[method];
            const vals = [d.avgTimeMs + ' мс', d.medianTimeMs + ' мс', d.p95TimeMs + ' мс', d.maxTimeMs + ' мс', d.count];
            return `${method}: ${vals[ctx.dataIndex]}`;
          },
        },
      },
    },
  };

  return (
    <div style={{ height: '400px', maxWidth: '600px', margin: '0 auto' }}>
      <Radar data={data} options={options} />
    </div>
  );
};

export default MethodComparison;
