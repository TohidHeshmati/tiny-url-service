"use client";

import {
    Bar,
    BarChart,
    CartesianGrid,
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
    startOfDay,
    startOfHour
} from "date-fns";

interface DataPoint {
    timestamp: string;
    clicks: number;
}

interface ClickChartProps {
    data: DataPoint[];
    granularity: "DAY" | "HOUR";
    startDate: string;
    endDate: string;
}

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

    // Generate all intervals
    const intervals = granularity === "DAY"
        ? eachDayOfInterval({ start, end })
        : eachHourOfInterval({ start, end });

    // Map intervals to data, filling with 0 if no data point exists
    const chartData = intervals.map((date) => {
        // Find matching data point
        const match = data.find((item) => {
            const itemDate = parseISO(item.timestamp);
            return granularity === "DAY"
                ? isSameDay(date, itemDate)
                : isSameHour(date, itemDate);
        });

        return {
            date: granularity === "HOUR" ? format(date, "HH:mm") : format(date, "MMM dd"),
            clicks: match ? match.clicks : 0,
            fullDate: format(date, "MMM dd, yyyy" + (granularity === "HOUR" ? " HH:mm" : "")),
        };
    });

    return (
        <div className="h-[300px] w-full">
            <h3 className="text-lg font-semibold text-gray-700 mb-4">Clicks Over Time</h3>
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
                                return (
                                    <div className="bg-white border border-gray-100 p-3 rounded shadow-lg">
                                        <p className="text-sm font-medium text-gray-900">{payload[0].payload.fullDate}</p>
                                        <div className="flex items-center gap-2 mt-1">
                                            <div className="w-2 h-2 rounded-full bg-blue-600" />
                                            <p className="text-sm text-gray-600">
                                                <span className="font-semibold text-gray-900">{payload[0].value}</span> clicks
                                            </p>
                                        </div>
                                    </div>
                                );
                            }
                            return null;
                        }}
                    />
                    <Bar
                        dataKey="clicks"
                        fill="#2563EB"
                        radius={[4, 4, 0, 0]}
                        maxBarSize={50}
                    />
                </BarChart>
            </ResponsiveContainer>
        </div>
    );
}