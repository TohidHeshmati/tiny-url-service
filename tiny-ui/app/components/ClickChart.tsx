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
import { format } from "date-fns";

interface DataPoint {
    timestamp: string;
    clicks: number;
}

interface ClickChartProps {
    data: DataPoint[];
}

export default function ClickChart({ data }: ClickChartProps) {
    if (!data || data.length === 0) {
        return (
            <div className="h-[300px] w-full flex items-center justify-center bg-gray-50 rounded-lg">
                <p className="text-gray-400">No data available for this period</p>
            </div>
        );
    }

    // Detect granularity based on time difference between first two points (heuristic)
    // or just format generically.
    // Let's check the distance between points.
    const isHourly = data.length > 1 &&
        (new Date(data[1].timestamp).getTime() - new Date(data[0].timestamp).getTime()) < 24 * 60 * 60 * 1000;

    // Format data for display
    const chartData = data.map((item) => {
        const date = new Date(item.timestamp);
        return {
            date: isHourly ? format(date, "HH:mm") : format(date, "MMM dd"),
            clicks: item.clicks,
            fullDate: format(date, "MMM dd, yyyy HH:mm"),
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
                        minTickGap={30} // Prevent overlap for hourly data
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