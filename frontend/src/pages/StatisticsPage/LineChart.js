import { Chart as ChartJS, Tooltip, CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  } from "chart.js";
import { Line } from "react-chartjs-2";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Tooltip,
);

const LineChart = ({statisticsData, statisticsLabel}) => {

  const option = {
    scales: {
      x : {
        ticks: {
          autoSkip: true,
          maxTicksLimit: 11
        },
        labels : statisticsLabel
      }
    },
    responsive: true,
    plugins: {
      legend: {
        display: false,
      },
    },
  }

  const topAvgData = {
    labels: statisticsLabel,
    datasets: [
      {
        data: statisticsData,
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.5)',
      },
    ],
  }

  return (
    <Line options={option} data={topAvgData} />
  );
}

export default LineChart;