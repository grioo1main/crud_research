import React from 'react';

const MetricsTable = ({ summary }) => {
  if (!summary || !summary.byMethod || Object.keys(summary.byMethod).length === 0) {
    return <p className="no-data">Нет данных для отображения</p>;
  }

  const methods = Object.keys(summary.byMethod);

  return (
    <table className="metrics-table">
      <thead>
        <tr>
          <th>HTTP-метод</th>
          <th>Запросов</th>
          <th>Среднее (мс)</th>
          <th>Медиана (мс)</th>
          <th>P95 (мс)</th>
          <th>Мин (мс)</th>
          <th>Макс (мс)</th>
          <th>Ошибки</th>
        </tr>
      </thead>
      <tbody>
        {methods.map((method) => {
          const d = summary.byMethod[method];
          return (
            <tr key={method}>
              <td>
                <span className={`method-badge ${method.toLowerCase()}`}>{method}</span>
              </td>
              <td>{d.count}</td>
              <td>{d.avgTimeMs}</td>
              <td>{d.medianTimeMs}</td>
              <td>{d.p95TimeMs}</td>
              <td>{d.minTimeMs}</td>
              <td>{d.maxTimeMs}</td>
              <td className={d.errors > 0 ? 'error' : ''}>{d.errors}</td>
            </tr>
          );
        })}
      </tbody>
      <tfoot>
        <tr>
          <td><strong>Общее</strong></td>
          <td><strong>{summary.totalRequests}</strong></td>
          <td><strong>{summary.avgResponseTimeMs}</strong></td>
          <td><strong>{summary.medianResponseTimeMs}</strong></td>
          <td><strong>{summary.p95ResponseTimeMs}</strong></td>
          <td colSpan="2"></td>
          <td><strong>{summary.errorRate}%</strong></td>
        </tr>
      </tfoot>
    </table>
  );
};

export default MetricsTable;
