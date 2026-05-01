import { useState } from "react";
import { shareFile } from "../api/fileApi";

export default function FileShare({ fileId, showToast, onDone, inline }) {
  const [recipient, setRecipient] = useState("");
  const [loading, setLoading] = useState(false);

  const handle = async () => {
    if (!recipient.trim()) return;
    setLoading(true);
    try {
      const res = await shareFile(fileId, recipient.trim());
      showToast(`✓ ${res.data}`, "success");
      setRecipient("");
      if (onDone) onDone();
    } catch (err) {
      showToast(err.response?.data || "Share failed.", "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={inline ? "share-inline" : "fc-share-row"} style={{ marginTop: 8 }}>
      <input
        className="share-input"
        placeholder="username or email…"
        value={recipient}
        onChange={(e) => setRecipient(e.target.value)}
        onKeyDown={(e) => e.key === "Enter" && handle()}
        autoFocus
        disabled={loading}
      />
      <button
        className="btn btn-teal btn-xs"
        onClick={handle}
        disabled={loading || !recipient.trim()}
      >
        {loading ? <span className="loading-spinner" /> : "Send"}
      </button>
    </div>
  );
}