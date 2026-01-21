"use client";
import { useState } from "react";
import StatsView from "./components/StatsView";

export default function Home() {
  const [activeTab, setActiveTab] = useState<"create" | "stats">("create");
  const [url, setUrl] = useState("");
  const [shortenedData, setShortenedData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleShorten = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setShortenedData(null);

    try {
      const response = await fetch("http://localhost:8080/api/v1/urls", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          original_url: url,
        }),
      });

      if (!response.ok) throw new Error("Failed to shorten URL");

      const data = await response.json();
      setShortenedData(data);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex flex-col items-center justify-center min-h-screen p-4 bg-gray-50">
      <div className="mb-8 flex gap-4 bg-white p-1 rounded-lg border shadow-sm">
        <button
          onClick={() => setActiveTab("create")}
          className={`px-6 py-2 rounded-md font-medium transition-colors ${activeTab === "create"
            ? "bg-blue-600 text-white shadow-sm"
            : "text-gray-600 hover:bg-gray-100"
            }`}
        >
          Create New
        </button>
        <button
          onClick={() => setActiveTab("stats")}
          className={`px-6 py-2 rounded-md font-medium transition-colors ${activeTab === "stats"
            ? "bg-blue-600 text-white shadow-sm"
            : "text-gray-600 hover:bg-gray-100"
            }`}
        >
          View Statistics
        </button>
      </div>

      {activeTab === "create" ? (
        <div className="w-full max-w-md p-8 bg-white rounded-xl shadow-lg">
          <h1 className="text-2xl font-bold text-center mb-6 text-blue-600">TinyURL Creator</h1>

          <form onSubmit={handleShorten} className="space-y-4">
            <input
              type="url"
              placeholder="Paste your long URL here..."
              required
              className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-400 outline-none text-blue-800"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
            />
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white p-3 rounded-lg font-semibold hover:bg-blue-700 disabled:bg-gray-400"
            >
              {loading ? "Shortening..." : "Shorten URL"}
            </button>
          </form>

          {error && <p className="mt-4 text-red-500 text-sm">{error}</p>}

          {shortenedData && (
            <div className="mt-8 p-4 bg-blue-50 border border-blue-100 rounded-lg">
              <p className="text-sm text-gray-600 mb-1">Your short URL:</p>
              <div className="flex items-center justify-between">
                <a
                  href={shortenedData.shortened_url}
                  target="_blank"
                  className="text-blue-700 font-medium break-all underline"
                >
                  {shortenedData.shortened_url}
                </a>
                <button
                  onClick={() => navigator.clipboard.writeText(shortenedData.shortened_url)}
                  className="ml-2 text-xs bg-white border px-2 py-1 rounded hover:bg-gray-100"
                >
                  Copy
                </button>
              </div>
            </div>
          )}
        </div>
      ) : (
        <StatsView />
      )}
    </main>
  );
}