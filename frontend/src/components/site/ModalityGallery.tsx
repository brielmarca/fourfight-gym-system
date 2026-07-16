type GalleryImage = {
  src: string;
  alt: string;
};

type ModalityGalleryProps = {
  heading: string;
  images: GalleryImage[];
};

export function ModalityGallery({ heading, images }: ModalityGalleryProps) {
  return (
    <section className="section-pad px-4">
      <div className="mx-auto max-w-6xl">
        <div className="mb-12 text-center">
          <h2
            className="font-display"
            style={{ fontSize: "clamp(36px, 6vw, 56px)", color: "#F5F5F5" }}
          >
            {heading}
          </h2>
          <div
            className="mx-auto mt-6"
            style={{ width: "48px", height: "2px", background: "#C1121F" }}
          />
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          {images.map((image) => (
            <div
              key={image.src}
              className="group relative aspect-[4/3] overflow-hidden rounded-lg border border-border-subtle transition-all duration-300 hover:border-red-500 hover:shadow-[0_0_24px_rgba(193,18,31,0.2)]"
            >
              <img
                src={image.src}
                alt={image.alt}
                loading="lazy"
                decoding="async"
                className="absolute inset-0 h-full w-full object-cover transition-transform duration-500 group-hover:scale-[1.01]"
              />
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
