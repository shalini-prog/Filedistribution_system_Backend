import { useState } from "react";
import { downloadFile } from "../api/fileApi";

export default function FileDownload({ fileId, filename }) {
  const [loading, setLoading] = useState(false);

  const handle = async () => {
    setLoading(true);
    try {
      await downloadFile(fileId, filename);
    } catch {
      alert("Download failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <button className="btn btn-ghost btn-sm" onClick={handle} disabled={loading}>
      {loading ? <span className="loading-spinner" /> : "↓"} Download
    </button>
  );
}