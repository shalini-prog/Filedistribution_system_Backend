import { useState } from "react";
import { deleteFile } from "../api/fileApi";
import FileShare from "./FileShare";
import FileDownload from "./FileDownload";

/* ── helpers ── */
function getIcon(filename) {
  if (!filename) return "📄";
  const ext = filename.split(".").pop().toLowerCase();
  const map = {
    pdf:"📕", png:"🖼️", jpg:"🖼️", jpeg:"🖼️", gif:"🖼️", svg:"🖼️", webp:"🖼️",
    mp4:"🎬", mov:"🎬", avi:"🎬", mp3:"🎵", wav:"🎵", flac:"🎵",
    zip:"📦", rar:"📦", tar:"📦", gz:"📦", "7z":"📦",
    js:"📜", ts:"📜", jsx:"📜", tsx:"📜", py:"🐍", java:"☕", go:"📜",
    doc:"📝", docx:"📝", xls:"📊", xlsx:"📊", csv:"📊",
    txt:"📄", md:"📄", html:"🌐", css:"🎨", json:"📋",
  };
  return map[ext] || "📄";
}

function fmtDate(d) {
  if (!d) return "—";
  return new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

/* 
  tabKey values:
    "uploaded"   → files[] are FileEntity  — show download, share, delete
    "sharedByMe" → files[] are FileShare   — show fileId, sharedWith, filename via sharedFileInfo
    "shared"     → files[] are FileEntity  — show download only
    "downloaded" → files[] are FileEntity  — show download only
*/
export default function FileList({ files, tabKey, onRefresh, onDeleteOptimistic, showToast }) {
  const [deleting, setDeleting] = useState(null);
  const isOwner    = tabKey === "uploaded";
  const isSharedByMe = tabKey === "sharedByMe";

  const handleDelete = async (fileId) => {
    if (!window.confirm("Delete this file permanently?")) return;
    // Optimistic: remove from UI immediately
    onDeleteOptimistic(fileId);
    setDeleting(fileId);
    try {
      await deleteFile(fileId);
      showToast("✓ File deleted.", "success");
      // Background sync to keep state accurate
      onRefresh();
    } catch (err) {
      showToast(err.response?.data || "Delete failed.", "error");
      // Re-fetch to restore if delete actually failed
      onRefresh();
    } finally {
      setDeleting(null);
    }
  };

  /* Empty states */
  if (!files || files.length === 0) {
    const info = {
      uploaded:   { icon: "📂", title: "No files yet",          sub: "Upload a file above to get started."           },
      sharedByMe: { icon: "📤", title: "Nothing shared yet",     sub: "Share a file from My Files to see it here."    },
      shared:     { icon: "🤝", title: "No shared files",        sub: "Nobody has shared files with you yet."         },
      downloaded: { icon: "⬇️", title: "No download history",    sub: "Files you download will appear here."          },
    }[tabKey] || { icon: "📄", title: "No files", sub: "" };
    return (
      <div className="empty-state">
        <div className="empty-icon">{info.icon}</div>
        <div className="empty-title">{info.title}</div>
        <div className="empty-sub">{info.sub}</div>
      </div>
    );
  }

  /* Column headers per tab */
  const colHeaders = isSharedByMe
    ? ["File", "Shared With", "Date", ""]
    : ["Filename", "Date", "Status", "Actions"];

  return (
    <>
      {/* ─── DESKTOP TABLE ─── */}
      <div className="file-table-wrap">
        <table className="file-table">
          <thead>
            <tr>
              {colHeaders.map((h, i) => (
                <th
                  key={i}
                  style={{ textAlign: i === colHeaders.length - 1 ? "right" : "left" }}
                >
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {files.map((file) =>
              isSharedByMe ? (
                <SharedByMeRow
                  key={file.id}
                  share={file}
                  showToast={showToast}
                />
              ) : (
                <FileRow
                  key={file.id}
                  file={file}
                  tabKey={tabKey}
                  isOwner={isOwner}
                  deleting={deleting}
                  onDelete={handleDelete}
                  showToast={showToast}
                />
              )
            )}
          </tbody>
        </table>
      </div>

      {/* ─── MOBILE CARDS ─── */}
      <div className="file-cards">
        {files.map((file) =>
          isSharedByMe ? (
            <SharedByMeCard key={file.id} share={file} showToast={showToast} />
          ) : (
            <FileCard
              key={file.id}
              file={file}
              tabKey={tabKey}
              isOwner={isOwner}
              deleting={deleting}
              onDelete={handleDelete}
              showToast={showToast}
            />
          )
        )}
      </div>
    </>
  );
}

/* ══════════════════════════════════════════
   Regular file rows/cards (uploaded / shared / downloaded)
══════════════════════════════════════════ */
function FileRow({ file, tabKey, isOwner, deleting, onDelete, showToast }) {
  const [showShare, setShowShare] = useState(false);
  return (
    <tr>
      <td style={{ width: "38%" }}>
        <div className="td-name">
          <div className="tbl-icon">{getIcon(file.filename)}</div>
          <div style={{ minWidth: 0 }}>
            <span className="tbl-filename" title={file.filename}>{file.filename}</span>
            {isOwner && showShare && (
              <FileShare
                fileId={file.id}
                showToast={showToast}
                onDone={() => setShowShare(false)}
              />
            )}
          </div>
        </div>
      </td>
      <td style={{ width: "16%" }}>
        <span className="tbl-date">{fmtDate(file.uploadDate)}</span>
      </td>
      <td style={{ width: "14%" }}>
        {tabKey === "shared"     && <span className="badge badge-yellow">Shared with me</span>}
        {tabKey === "downloaded" && <span className="badge badge-teal">Downloaded</span>}
      </td>
      <td style={{ width: "32%", textAlign: "right" }}>
        <div className="td-actions">
          <FileDownload fileId={file.id} filename={file.filename} />
          {isOwner && (
            <button
              className="btn btn-teal btn-sm"
              onClick={() => setShowShare(v => !v)}
            >
              {showShare ? "✕ Cancel" : "⇗ Share"}
            </button>
          )}
          {isOwner && (
            <button
              className="btn btn-danger btn-sm"
              onClick={() => onDelete(file.id)}
              disabled={deleting === file.id}
            >
              {deleting === file.id ? <span className="loading-spinner" /> : "🗑 Delete"}
            </button>
          )}
        </div>
      </td>
    </tr>
  );
}

function FileCard({ file, tabKey, isOwner, deleting, onDelete, showToast }) {
  const [showShare, setShowShare] = useState(false);
  return (
    <div className="file-card">
      <div className="file-card-top">
        <div className="fc-icon">{getIcon(file.filename)}</div>
        <div className="fc-body">
          <div className="fc-name" title={file.filename}>{file.filename}</div>
          <div className="fc-meta">
            {fmtDate(file.uploadDate)}
            {tabKey === "shared"     && <span className="badge badge-yellow">Shared with me</span>}
            {tabKey === "downloaded" && <span className="badge badge-teal">Downloaded</span>}
          </div>
          {isOwner && showShare && (
            <FileShare
              fileId={file.id}
              showToast={showToast}
              onDone={() => setShowShare(false)}
            />
          )}
        </div>
      </div>
      <div className="fc-actions">
        <FileDownload fileId={file.id} filename={file.filename} />
        {isOwner && (
          <button
            className="btn btn-teal btn-sm"
            onClick={() => setShowShare(v => !v)}
          >
            {showShare ? "✕ Cancel" : "⇗ Share"}
          </button>
        )}
        {isOwner && (
          <button
            className="btn btn-danger btn-sm"
            onClick={() => onDelete(file.id)}
            disabled={deleting === file.id}
          >
            {deleting === file.id ? <span className="loading-spinner" /> : "🗑 Delete"}
          </button>
        )}
      </div>
    </div>
  );
}

/* ══════════════════════════════════════════
   Shared-by-me rows/cards  (FileShare objects)
  FileShare shape: { id, fileId, sharedBy, sharedWith, filename?, sharedWithUsername? }
══════════════════════════════════════════ */
function SharedByMeRow({ share }) {
  /* The backend /shared-by-me returns FileShare entities.
     We display what we have — fileId as fallback if no filename. */
  const filename        = share.filename        || `File #${share.fileId}`;
  const recipientLabel  = share.sharedWithUsername || share.sharedWithEmail || `User #${share.sharedWith}`;

  return (
    <tr>
      <td style={{ width: "36%" }}>
        <div className="td-name">
          <div className="tbl-icon">{getIcon(filename)}</div>
          <span className="tbl-filename" title={filename}>{filename}</span>
        </div>
      </td>
      <td style={{ width: "28%" }}>
        <span className="shared-with-pill">
          <span style={{ marginRight: 5 }}>👤</span>
          {recipientLabel}
        </span>
      </td>
      <td style={{ width: "16%" }}>
        <span className="tbl-date">{fmtDate(share.sharedAt || share.createdAt)}</span>
      </td>
      <td style={{ width: "20%", textAlign: "right" }}>
        <span className="badge badge-yellow">Shared</span>
      </td>
    </tr>
  );
}

function SharedByMeCard({ share }) {
  const filename       = share.filename        || `File #${share.fileId}`;
  const recipientLabel = share.sharedWithUsername || share.sharedWithEmail || `User #${share.sharedWith}`;

  return (
    <div className="file-card">
      <div className="file-card-top">
        <div className="fc-icon">{getIcon(filename)}</div>
        <div className="fc-body">
          <div className="fc-name" title={filename}>{filename}</div>
          <div className="fc-meta">
            <span>📤 Shared with </span>
            <span className="shared-with-pill">👤 {recipientLabel}</span>
          </div>
          <div className="fc-meta" style={{ marginTop: 3 }}>
            {fmtDate(share.sharedAt || share.createdAt)}
            <span className="badge badge-yellow">Shared</span>
          </div>
        </div>
      </div>
    </div>
  );
}