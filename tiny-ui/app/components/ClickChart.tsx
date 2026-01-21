"use client";

import {
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    Legend,
    Pie,
    PieChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";
import {
    format,
    eachDayOfInterval,
    eachHourOfInterval,
    isSameDay,
    isSameHour,
    parseISO,
} from "date-fns";

interface DataPoint {
    timestamp: string;
    clicks: number;
    deviceClicks?: Record<string, number>;
}

interface ClickChartProps {
    data: DataPoint[];
    granularity: "DAY" | "HOUR" | "MONTH";
    startDate: string;
    endDate: string;
}

const DEVICE_COLORS = {
    DESKTOP: "#2563EB", // Blue
    MOBILE: "#16A34A",  // Green
    TABLET: "#9333EA",  // Purple
    OTHER: "#9CA3AF",   // Gray
};

export default function ClickChart({ data, granularity, startDate, endDate }: ClickChartProps) {
    if (!startDate || !endDate) {
        return (
            <div className="h-[300px] w-full flex items-center justify-center bg-gray-50 rounded-lg">
                <p className="text-gray-400">No data available for this period</p>
            </div>
        );
    }

    const start = parseISO(startDate);
    const end = parseISO(endDate);

    if (granularity === "MONTH") {
        // Aggregate totals by device type across the entire range
        const totals = {
            DESKTOP: 0,
            MOBILE: 0,
            TABLET: 0,
            OTHER: 0,
        };

        data.forEach(item => {
            // device_clicks can be snake_case from backend
            const devices = (item as DataPoint & { device_clicks?: Record<string, number> })?.device_clicks || item.deviceClicks || {};
            totals.DESKTOP += devices["DESKTOP"] || 0;
            totals.MOBILE += devices["MOBILE"] || 0;
            totals.TABLET += devices["TABLET"] || 0;
            totals.OTHER += devices["OTHER"] || 0;
        });

        const pieData = Object.entries(totals)
            .map(([device, value]) => ({
                name: device.charAt(0) + device.slice(1).toLowerCase(), // "Desktop"
                value,
                color: DEVICE_COLORS[device as keyof typeof DEVICE_COLORS]
            }))
            .filter(d => d.value > 0);

        return (
            <div className="h-[300px] w-full">
                <h3 className="text-lg font-semibold text-gray-700 mb-4">Device Distribution</h3>
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
                            data={pieData}
                            cx="50%"
                            cy="50%"
                            labelLine={false}
                            label={({ name, percent }: { name?: string; percent?: number }) => `${name || "Unknown"} ${((percent || 0) * 100).toFixed(0)}%`}
                            outerRadius={100}
                            dataKey="value"
                        >
                            {pieData.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={entry.color} />
                            ))}
                        </Pie>
                        <Tooltip />
                        <Legend />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        );
    }

    // Generate all intervals
    const intervals = granularity === "HOUR"
        ? eachHourOfInterval({ start, end })
        : eachDayOfInterval({ start, end });

    // Map intervals to data, filling with 0 if no data point exists
    const chartData = intervals.map((date) => {
        // Find matching data point
        const match = data.find((item) => {
            const itemDate = parseISO(item.timestamp);
            return granularity === "HOUR"
                ? isSameHour(date, itemDate)
                : isSameDay(date, itemDate);
        });

        // backend returns device_clicks (snake_case)
        const devices = (match as DataPoint & { device_clicks?: Record<string, number> })?.device_clicks || match?.deviceClicks || {};

        return {
            date: granularity === "HOUR" ? format(date, "HH:mm") : format(date, "MMM dd"),
            fullDate: format(date, "MMM dd, yyyy" + (granularity === "HOUR" ? " HH:mm" : "")),
            clicks: match ? match.clicks : 0,
            // Spread device clicks so Recharts can access them keys
            DESKTOP: devices["DESKTOP"] || 0,
            MOBILE: devices["MOBILE"] || 0,
            TABLET: devices["TABLET"] || 0,
            OTHER: devices["OTHER"] || 0,
        };
    });

    return (
        <div className="h-[300px] w-full">
            <h3 className="text-lg font-semibold text-gray-700 mb-4">Clicks by Device</h3>
            <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                    <XAxis
                        dataKey="date"
                        tickLine={false}
                        axisLine={false}
                        tick={{ fontSize: 12, fill: '#6B7280' }}
                        dy={10}
                        minTickGap={30}
                    />
                    <YAxis
                        tickLine={false}
                        axisLine={false}
                        tick={{ fontSize: 12, fill: '#6B7280' }}
                        allowDecimals={false}
                    />
                    <Tooltip
                        cursor={{ fill: 'transparent' }}
                        content={({ active, payload }) => {
                            if (active && payload && payload.length) {
                                const data = payload[0].payload;
                                const total = data.clicks;

                                return (
                                    <div className="bg-white border border-gray-100 p-3 rounded shadow-lg min-w-[150px]">
                                        <p className="text-sm font-medium text-gray-900 mb-2">{data.fullDate}</p>
                                        <div className="space-y-1">
                                            {Object.entries(DEVICE_COLORS).map(([device, color]) => (
                                                <div key={device} className="flex items-center justify-between gap-4 text-xs">
                                                    <div className="flex items-center gap-1.5">
                                                        <div className="w-2 h-2 rounded-full" style={{ backgroundColor: color }} />
                                                        <span className="text-gray-600 capitalize">{device.toLowerCase()}</span>
                                                    </div>
                                                    <span className="font-medium text-gray-900">{data[device]}</span>
                                                </div>
                                            ))}
                                            <div className="border-t pt-1 mt-1 flex justify-between gap-4 text-xs font-semibold">
                                                <span>Total</span>
                                                <span>{total}</span>
                                            </div>
                                        </div>
                                    </div>
                                );
                            }
                            return null;
                        }}
                    />
                    <Legend
                        verticalAlign="top"
                        height={36}
                        iconType="circle"
                        iconSize={8}
                        wrapperStyle={{ fontSize: '12px' }}
                    />
                    <Bar dataKey="DESKTOP" stackId="device" fill={DEVICE_COLORS.DESKTOP} name="Desktop" radius={[0, 0, 0, 0]} />
                    <Bar dataKey="MOBILE" stackId="device" fill={DEVICE_COLORS.MOBILE} name="Mobile" radius={[0, 0, 0, 0]} />
                    <Bar dataKey="TABLET" stackId="device" fill={DEVICE_COLORS.TABLET} name="Tablet" radius={[0, 0, 0, 0]} />
                    <Bar dataKey="OTHER" stackId="device" fill={DEVICE_COLORS.OTHER} name="Other" radius={[4, 4, 0, 0]} />
                </BarChart>
            </ResponsiveContainer>
        </div>
    );
}