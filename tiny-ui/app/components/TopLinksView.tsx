"use client";

import { useEffect, useState } from "react";


interface UrlSummary {
    short_code: string;
    original_url: string;
    total_clicks: number;
}

export default function TopLinksView() {
    const [topLinks, setTopLinks] = useState<UrlSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchTopLinks = async () => {
            try {
                const response = await fetch("http://localhost:8080/api/v1/urls/stats/top");
                if (!response.ok) {
                    throw new Error("Failed to fetch top links");
                }
                const data = await response.json();
                setTopLinks(data);
            } catch (err: unknown) {
                setError(err instanceof Error ? err.message : "An unknown error occurred");
            } finally {
                setLoading(false);
            }
        };

        fetchTopLinks();
    }, []);

    if (loading) {
        return (
            <div className="w-full max-w-4xl p-8 bg-white rounded-xl shadow-lg flex justify-center">
                <p className="text-gray-500">Loading top links...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="w-full max-w-4xl p-8 bg-white rounded-xl shadow-lg flex justify-center">
                <p className="text-red-500">{error}</p>
            </div>
        );
    }

    return (
        <div className="w-full max-w-4xl p-8 bg-white rounded-xl shadow-lg">
            <h2 className="text-2xl font-bold text-center mb-6 text-blue-600">Top 10 Most Clicked LInks</h2>

            <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                    <thead>
                        <tr className="border-b border-gray-200">
                            <th className="py-4 px-4 font-semibold text-gray-700">Rank</th>
                            <th className="py-4 px-4 font-semibold text-gray-700">Short Code</th>
                            <th className="py-4 px-4 font-semibold text-gray-700">Total Clicks</th>
                            <th className="py-4 px-4 font-semibold text-gray-700">Original URL</th>
                        </tr>
                    </thead>
                    <tbody>
                        {topLinks.map((link, index) => (
                            <tr
                                key={link.short_code}
                                className="border-b border-gray-100 hover:bg-blue-50 transition-colors"
                            >
                                <td className="py-3 px-4 text-gray-500 font-medium">#{index + 1}</td>
                                <td className="py-3 px-4">
                                    <span className="bg-blue-100 text-blue-800 py-1 px-3 rounded-full text-sm font-medium">
                                        {link.short_code}
                                    </span>
                                </td>
                                <td className="py-3 px-4 font-bold text-gray-800">
                                    {link.total_clicks.toLocaleString()}
                                </td>
                                <td className="py-3 px-4 text-gray-600 max-w-xs truncate" title={link.original_url}>
                                    {link.original_url}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {topLinks.length === 0 && (
                <div className="text-center py-8 text-gray-500">
                    No links found yet.
                </div>
            )}
        </div>
    );
}
