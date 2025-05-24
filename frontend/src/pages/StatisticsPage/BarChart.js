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
	const filterData = datas[tab].filter((el) => {
		if (tab === 5) { // 학생생활관 탭은 시간대 필터링을 하지 않음
			return true;
		} else {
			return el.time >= "10:00" && el.time <= "15:00";
		}
	})
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
					maxTicksLimit: (tab === 5 ? 25 : 11),
				},
				labels: statisticsLabel,
			},
		},
		responsive: false,
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

	return <Bar options={option} data={topAvgData} height={600} style={{ position: "relative", height: tab === 5 ? "700px" : "600px", width: "300px"}}/>;
};

export default BarChart;
