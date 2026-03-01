import React from 'react';
import { Bar } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const ThroughputChart = ({ summary }) => {
  if (!summary || !summary.byMethod || Object.keys(summary.byMethod).length === 0) {
    return <p className="no-data">Нет данных для отображения</p>;
  }

  const methods = Object.keys(summary.byMethod);
  const colors = {
    GET: '#22c55e',
    POST: '#3b82f6',
    PUT: '#f97316',
    DELETE: '#ef4444',
  };

  const data = {
    labels: methods,
    datasets: [
      {
        label: 'Количество запросов',
        data: methods.map((m) => summary.byMethod[m].count),
        backgroundColor: methods.map((m) => colors[m] || '#888'),
        borderRadius: 6,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
    },
    scales: {
      y: {
        beginAtZero: true,
        title: { display: true, text: 'Количество запросов' },
      },
    },
  };

  return (
    <div style={{ height: '350px' }}>
      <Bar data={data} options={options} />
    </div>
  );
};

export default ThroughputChart;
