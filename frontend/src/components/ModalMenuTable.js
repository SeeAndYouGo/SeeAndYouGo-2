import styled from "@emotion/styled";
import React from "react";


const Table = styled.table`
    width: 80%;
    border-collapse: collapse;
    text-align: center;
    margin: 0px auto;

    & tbody th, td {
        font-size: 14px;
    }
    &:first-of-type {
        margin-bottom: 10px;
    }

`;


// props로 menu정보를 가져오는 것이 좋아보임
const ModalMenuTable = () => {
    const days = [
        "월요일",
        "화요일",
        "수요일",
        "목요일",
        "금요일",
    ];
    const menuTable = [
        [
            "청국장찌개",
            "불고기뚝배기",
            "설렁탕",
            "보리열무비빔밥",
            "미니샤브전골"
        ],
        [
            "오므라이스",
            "치즈돈까스",
            "불고기덮밥",
            "김치찌개",
            "로제스파게티"
        ]
    ];

    const MenuTable = ({dept}) => {
        return(
            <Table>
            <thead style={{border: "none"}}>
                <tr>
                    <th colSpan={2} style={{textAlign: "left"}}>
                        {dept == 0 ? "교직원식당" : "학생식당"}
                    </th>
                </tr>
            </thead>
            <tbody>
                {days.map((day, index) => {
                    return <tr key={index}>
                        <th key={day} style={{padding: "5px", border: "1px solid black"}}>{day}</th>
                        <td key={menuTable[dept][index]} style={{padding:"5px 20px", border: "1px solid black", fontWeight:400}}>{menuTable[dept][index]}</td>
                    </tr>
                })}
            </tbody>
        </Table>
        )
    }

    return (
        <>
            <MenuTable dept={0} />
            <MenuTable dept={1} />
        </>
    )
};

export default ModalMenuTable;

