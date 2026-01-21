"use client";

import { useState } from "react";
import ClickChart from "./ClickChart";
import { format } from "date-fns";

type Granularity = "DAY" | "HOUR";

export default function StatsView() {
    const [shortCode, setShortCode] = useState("");
    const [granularity, setGranularity] = useState<Granularity>("DAY");
    const [stats, setStats] = useState<any>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleFetchStats = async (e?: React.FormEvent) => {
        if (e) e.preventDefault();
        if (!shortCode) return;

        setLoading(true);
        setError("");
        // specific fix: keep stats while loading new granularity to avoid flickering if desired, 
        // but clearing it makes it obvious data is refreshing.
        setStats(null);

        try {
            const response = await fetch(`http://localhost:8080/api/v1/urls/${shortCode}/stats?granularity=${granularity}`);

            if (!response.ok) {
                if (response.status === 404) throw new Error("Short URL not found");
                throw new Error("Failed to fetch statistics");
            }

            const data = await response.json();
            setStats(data);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };



    // Effect-like behavior for auto-refreshing when granularity changes could be complex 
    // without useEffect. Let's just make the user click "View Stats" again or 
    // include the granularity selector IN the form or run it immediately.
    // Let's modify the UI to allow selecting BEFORE fetching.

    return (
        <div className="w-full max-w-2xl p-8 bg-white rounded-xl shadow-lg">
            <h2 className="text-2xl font-bold text-center mb-6 text-blue-600">URL Analytics</h2>

            <form onSubmit={handleFetchStats} className="flex flex-col gap-4 mb-8">
                <div className="flex gap-2">
                    <input
                        type="text"
                        placeholder="Enter short code (e.g., abc12)"
                        required
                        className="flex-1 p-3 border rounded-lg focus:ring-2 focus:ring-blue-400 outline-none"
                        value={shortCode}
                        onChange={(e) => setShortCode(e.target.value)}
                    />
                    <select
                        value={granularity}
                        onChange={(e) => setGranularity(e.target.value as Granularity)}
                        className="p-3 border rounded-lg focus:ring-2 focus:ring-blue-400 outline-none bg-white"
                    >
                        <option value="DAY">Daily</option>
                        <option value="HOUR">Hourly</option>
                    </select>
                </div>

                <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400"
                >
                    {loading ? "Loading..." : "View Stats"}
                </button>
            </form>

            {error && <p className="text-red-500 text-center mb-4">{error}</p>}

            {stats && (
                <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div className="bg-blue-50 p-4 rounded-xl border border-blue-100">
                            <p className="text-sm text-gray-500 mb-1">Total Clicks</p>
                            <p className="text-3xl font-bold text-blue-700">{stats.total_clicks}</p>
                        </div>
                        <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                            <p className="text-sm text-gray-500 mb-1">Created At</p>
                            <p className="text-lg font-medium text-gray-700">
                                {format(new Date(stats.created_at), "MMM dd, yyyy")}
                            </p>
                        </div>
                        <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                            <p className="text-sm text-gray-500 mb-1">Long URL</p>
                            <p className="text-sm font-medium text-gray-700 truncate" title={stats.long_url}>
                                {stats.long_url}
                            </p>
                        </div>
                    </div>

                    <div className="bg-white border rounded-xl p-4">
                        <ClickChart data={stats.time_series.data_points} />
                    </div>
                </div>
            )}
        </div>
    );
}
