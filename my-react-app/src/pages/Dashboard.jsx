import { useState, useEffect, useCallback } from "react";
import { listFiles, getSharedByMe, logout } from "../api/fileApi";
import FileUpload from "../components/FileUpload";
import FileList from "../components/FileList";

function Toast({ message, type, onClose }) {
  useEffect(() => {
    const t = setTimeout(onClose, 3500);
    return () => clearTimeout(t);
  }, [onClose]);
  return (
    <div className={`toast toast-${type}`} onClick={onClose}>
      {message}
    </div>
  );
}

const NAV = [
  { key: "uploaded",    label: "My Files",        icon: "📂",
    sub: "Files you have uploaded. You can share or delete them." },
  { key: "sharedByMe",  label: "Shared By Me",     icon: "📤",
    sub: "Files you have shared with other users." },
  { key: "shared",      label: "Shared With Me",   icon: "🤝",
    sub: "Files other users have shared with you." },
  { key: "downloaded",  label: "Download History", icon: "⬇️",
    sub: "Your file download history." },
];

export default function Dashboard({ onLogout }) {
  const [data, setData] = useState({
    uploadedFiles:   [],
    sharedFiles:     [],
    downloadedFiles: [],
    sharedByMeFiles: [],   // FileShare objects from /shared-by-me
  });
  const [loading,   setLoading]   = useState(true);
  const [toast,     setToast]     = useState(null);
  const [activeTab, setActiveTab] = useState("uploaded");

  const getUsername = () => {
    const token = localStorage.getItem("jwt");
    if (!token) return null;
    try { return JSON.parse(atob(token.split(".")[1])).sub; } catch { return null; }
  };
  const username = getUsername();

  const showToast = (message, type = "success") =>
    setToast({ message, type, key: Date.now() });

  const fetchAll = useCallback(async () => {
    setLoading(true);
    try {
      const [listRes, sharedByMeRes] = await Promise.all([
        listFiles(),
        getSharedByMe(),
      ]);
      setData({
        uploadedFiles:   listRes.data.uploadedFiles   ?? [],
        sharedFiles:     listRes.data.sharedFiles     ?? [],
        downloadedFiles: listRes.data.downloadedFiles ?? [],
        sharedByMeFiles: sharedByMeRes.data           ?? [],
      });
    } catch {
      showToast("Failed to load files.", "error");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  // Optimistic delete — remove from local state instantly, then re-sync
  const handleDeleteOptimistic = (fileId) => {
    setData(prev => ({
      ...prev,
      uploadedFiles: prev.uploadedFiles.filter(f => f.id !== fileId),
    }));
  };

  const handleLogout = async () => {
    try { await logout(); } catch {}
    onLogout();
  };

  const counts = {
    uploaded:   data.uploadedFiles.length,
    sharedByMe: data.sharedByMeFiles.length,
    shared:     data.sharedFiles.length,
    downloaded: data.downloadedFiles.length,
  };

  const activeFiles = {
    uploaded:   data.uploadedFiles,
    sharedByMe: data.sharedByMeFiles,
    shared:     data.sharedFiles,
    downloaded: data.downloadedFiles,
  }[activeTab] || [];

  const activeNav = NAV.find(n => n.key === activeTab);

  return (
    <div className="dashboard">

      {/* ── Top bar ── */}
      <header className="topbar">
        <div className="topbar-logo">DFS<em>/</em>VAULT</div>
        <div className="topbar-right">
          {username && <span className="topbar-user">@{username}</span>}
          <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </header>

      {/* ── Mobile tab bar ── */}
      <div className="mobile-tabs">
        {NAV.map((n) => (
          <button
            key={n.key}
            className={`mob-tab ${activeTab === n.key ? "active" : ""}`}
            onClick={() => setActiveTab(n.key)}
          >
            {n.icon} {n.label}
            <span className="mob-tab-count">{counts[n.key]}</span>
          </button>
        ))}
      </div>

      {/* ── Body ── */}
      <div className="layout-body">

        {/* ── Sidebar (desktop) ── */}
        <aside className="sidebar">
          <span className="sidebar-section-label">Navigation</span>
          {NAV.map((n) => (
            <button
              key={n.key}
              className={`sidebar-item ${activeTab === n.key ? "active" : ""}`}
              onClick={() => setActiveTab(n.key)}
            >
              <span className="sidebar-item-icon">{n.icon}</span>
              {n.label}
              <span className="sidebar-count">{counts[n.key]}</span>
            </button>
          ))}
        </aside>

        {/* ── Main ── */}
        <main className="main">
          <div className="page-header">
            <h1 className="page-title">{activeNav?.icon} {activeNav?.label}</h1>
            <p className="page-sub">{activeNav?.sub}</p>
          </div>

          {/* Upload only on My Files tab */}
          {activeTab === "uploaded" && (
            <FileUpload onUploaded={fetchAll} showToast={showToast} />
          )}

          {loading ? (
            <div className="center-spinner">
              <span className="loading-spinner loading-spinner-lg" />
            </div>
          ) : (
            <FileList
              files={activeFiles}
              tabKey={activeTab}
              onRefresh={fetchAll}
              onDeleteOptimistic={handleDeleteOptimistic}
              showToast={showToast}
            />
          )}
        </main>
      </div>

      {toast && (
        <Toast
          key={toast.key}
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
}