import { useState, useRef } from "react";
import { uploadChunked } from "../api/fileApi";

export default function FileUpload({ onUploaded, showToast }) {
  const [file,      setFile]      = useState(null);
  const [progress,  setProgress]  = useState(0);
  const [uploading, setUploading] = useState(false);
  const inputRef = useRef();

  const handleUpload = async () => {
    if (!file) return;
    setUploading(true);
    setProgress(0);
    try {
      await uploadChunked(file, setProgress);
      showToast(`✓ Uploaded: ${file.name}`, "success");
      setFile(null);
      if (inputRef.current) inputRef.current.value = "";
      onUploaded();
    } catch (err) {
      showToast(err.response?.data || "Upload failed.", "error");
    } finally {
      setUploading(false);
      setProgress(0);
    }
  };

  return (
    <div className="upload-section">
      <div className="section-title">Upload a new file</div>
      <div className="upload-row">
        <label className="file-drop-zone">
          <span className="file-drop-zone-icon">📁</span>
          <span>{file ? "Change file" : "Choose file to upload…"}</span>
          <input
            ref={inputRef}
            type="file"
            onChange={(e) => setFile(e.target.files[0])}
            disabled={uploading}
          />
        </label>

        {file && (
          <span className="selected-name" title={file.name}>
            {file.name}&nbsp;·&nbsp;{(file.size / 1024).toFixed(1)} KB
          </span>
        )}

        <div className="upload-btns">
          <button
            className="btn btn-ghost"
            onClick={handleUpload}
            disabled={!file || uploading}
            title="Split into 1 MB chunks for distributed storage"
          >
            {uploading && <span className="loading-spinner" />}
            Upload (Chunked)
          </button>
        </div>
      </div>

      {uploading && (
        <>
          <div className="progress-bar-wrap">
            <div className="progress-bar" style={{ width: `${progress}%` }} />
          </div>
          <div className="progress-label">{progress}% — uploading…</div>
        </>
      )}
    </div>
  );
}