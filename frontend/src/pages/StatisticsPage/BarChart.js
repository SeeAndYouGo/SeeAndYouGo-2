import {
	Chart as ChartJS,
	CategoryScale,
	LinearScale,
	BarElement,
	Tooltip,
} from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip);

const BarChart = ({ datas, tab }) => {
	const filterData = datas[tab] === 5 ? datas[tab] : datas[tab].filter((el) => el.time >= "10:00" && el.time <= "15:00");
	const statisticsLabel = filterData.map((el) => el.time);
	const statisticsData = filterData.map((el) => el.averageValue);

	const option = {
		indexAxis: "y",
		scales: {
			x: {
				position: "top",
			},
			y: {
				position: "left",
				ticks: {
					autoSkip: true,
					maxTicksLimit: 11,
				},
				labels: statisticsLabel,
			},
		},
		responsive: true,
		plugins: {
			legend: {
				display: false,
			},
		},
	};

	const topAvgData = {
		labels: statisticsLabel,
		datasets: [
			{
				data: statisticsData,
				label: "평균 인원 수",
				borderColor: "rgb(75, 192, 192)",
				backgroundColor: "rgba(75, 192, 192, 0.5)",
			},
		],
	};

	return <Bar options={option} data={topAvgData} height={600} />;
};

export default BarChart;
