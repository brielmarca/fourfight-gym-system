import { useState, useEffect } from "react";

const ACADEMY_COORDS: [number, number] = [41.1401, -8.535];

interface DarkMapProps {
  height?: string;
  className?: string;
}

function LeafletMap({ height, className }: DarkMapProps) {
  const { MapContainer, TileLayer, Marker, Popup } = require("react-leaflet");
  const L = require("leaflet");
  require("leaflet/dist/leaflet.css");

  const customIcon = L.divIcon({
    className: "custom-marker",
    html: `<div style="background: #C1121F; width: 16px; height: 16px; border-radius: 50%; border: 3px solid #0B0B0B; box-shadow: 0 0 12px rgba(193, 18, 31, 0.6);"></div>`,
    iconSize: [16, 16],
    iconAnchor: [8, 8],
  });

  return (
    <div
      className={`rounded-xl overflow-hidden border border-border-subtle ${className}`}
      style={{ height }}
    >
      <MapContainer
        center={ACADEMY_COORDS}
        zoom={14}
        scrollWheelZoom={false}
        style={{ height: "100%", width: "100%", background: "#0B0B0B" }}
        className="leaflet-dark-map"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> | &copy; <a href="https://carto.com/">CARTO</a>'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />
        <Marker position={ACADEMY_COORDS} icon={customIcon}>
          <Popup>
            <span style={{ color: "#0B0B0B", fontWeight: 600 }}>4Four Fight Academy</span>
          </Popup>
        </Marker>
      </MapContainer>
    </div>
  );
}

export function DarkMap({ height = "380px", className = "" }: DarkMapProps) {
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
  }, []);

  if (!isClient) {
    return (
      <div
        className={`rounded-xl overflow-hidden border border-border-subtle bg-muted flex items-center justify-center ${className}`}
        style={{ height }}
      >
        <div className="text-muted-foreground text-sm">Loading map...</div>
      </div>
    );
  }

  return <LeafletMap height={height} className={className} />;
}
