import React, { useState, useEffect } from "react";
import "rsuite/dist/rsuite-no-reset.min.css";
import { Cascader } from "rsuite";

const MenuSelector = ({ onSelectMenu }) => {
    const [menuData, setMenuData] = useState([]);

    const handleMenuClick = (value) => {
        onSelectMenu(value);
    };

    useEffect(() => {
        const fetchData = async () => {
            const res = await fetch(`/assets/json/Restaurant1Menu.json`, {
                headers: {
                    "Content-Type": "application/json",
                },
                method: "GET",
            });
            const result = await res.json();
            return result;
        };
        fetchData().then((data) => {
            setMenuData(data);
        });
    }, []);

    return (
        <div style={{ display: "block", marginBottom: 10 }}>
            <p style={{ margin: "0", float: "left", fontSize: 15 }}>
                메뉴 선택
            </p>
            <Cascader
                style={{ width: "100%", marginTop: 5 }}
                placeholder="메뉴를 선택해주세요"
                data={menuData}
                onChange={(value) => {
                    handleMenuClick(value);
                }}
            />
        </div>
    );
};

export default MenuSelector;