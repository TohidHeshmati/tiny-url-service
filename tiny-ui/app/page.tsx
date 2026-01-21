"use client";
import { useState } from "react";
import StatsView from "./components/StatsView";
import TopLinksView from "./components/TopLinksView";
import { QRCodeCanvas } from "qrcode.react";

export default function Home() {
  const [activeTab, setActiveTab] = useState<"create" | "stats" | "top10">("create");
  const [url, setUrl] = useState("");
  const [shortenedData, setShortenedData] = useState<{ shortened_url: string } | null>(null);
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
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : "An unknown error occurred");
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
        <button
          onClick={() => setActiveTab("top10")}
          className={`px-6 py-2 rounded-md font-medium transition-colors ${activeTab === "top10"
            ? "bg-blue-600 text-white shadow-sm"
            : "text-gray-600 hover:bg-gray-100"
            }`}
        >
          Top 10 Links
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
            <div className="mt-8 p-6 bg-blue-50 border border-blue-100 rounded-xl">
              <p className="text-sm font-medium text-blue-900 mb-3">Your short URL is ready!</p>

              <div className="flex flex-col md:flex-row gap-6 items-center">
                <div className="flex-1 w-full">
                  <div className="flex items-center justify-between bg-white p-3 rounded-lg border border-blue-200">
                    <a
                      href={shortenedData.shortened_url}
                      target="_blank"
                      className="text-blue-700 font-medium break-all hover:underline truncate mr-2"
                    >
                      {shortenedData.shortened_url}
                    </a>
                    <button
                      onClick={() => {
                        navigator.clipboard.writeText(shortenedData.shortened_url);
                        // Optional: Add a toast notification here
                      }}
                      className="text-xs bg-gray-50 text-gray-700 border border-gray-200 px-3 py-1.5 rounded-md hover:bg-gray-100 hover:text-gray-900 font-medium transition-colors whitespace-nowrap"
                    >
                      Copy Link
                    </button>
                  </div>
                </div>

                <div className="flex flex-col items-center gap-3 bg-white p-4 rounded-lg border border-blue-200 shadow-sm">
                  <QRCodeCanvas
                    value={shortenedData.shortened_url}
                    size={128}
                    level={"H"}
                    includeMargin={true}
                    id="qr-code-canvas"
                  />
                  <button
                    onClick={() => {
                      const canvas = document.getElementById('qr-code-canvas') as HTMLCanvasElement;
                      if (canvas) {
                        const pngUrl = canvas.toDataURL("image/png");
                        const downloadLink = document.createElement("a");
                        downloadLink.href = pngUrl;
                        downloadLink.download = `qr-code-${shortenedData.shortened_url.split('/').pop()}.png`;
                        document.body.appendChild(downloadLink);
                        downloadLink.click();
                        document.body.removeChild(downloadLink);
                      }
                    }}
                    className="text-xs text-blue-600 hover:text-blue-800 font-medium flex items-center gap-1"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="7 10 12 15 17 10" /><line x1="12" x2="12" y1="15" y2="3" /></svg>
                    Download QR
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      ) : activeTab === "stats" ? (
        <StatsView />
      ) : (
        <TopLinksView />
      )}
    </main>
  );
}