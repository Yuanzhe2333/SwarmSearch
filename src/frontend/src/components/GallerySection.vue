<template>
  <div class="container">
    <div class="gallery_container">
      <div
        v-for="(item, index) in results"
        :key="`${item['_source'].image_url}-${searchTimestamp}`"
        class="gallery_card"
      >
        <div class="card_content">
          <div class="text_container">
            <a
              :href="`${item['_source'].image_url}`"
              target="_blank"
              rel="noopener noreferrer"
              class="custom_link"
            >
              <h2 class="card_title">
                {{ item["_source"].title || "No Title" }}
              </h2>
            </a>

            <!-- image_url is the actual url for now -->
            <h5 class="card_url">{{ item["_source"].image_url }}</h5>
            <p class="card_description">{{ item["_source"].explanation }}</p>
            <span class="card_date">{{ item["_source"].date }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ImagePopover from "./ImagePopover.vue";

export default {
  name: "GallerySection",
  components: {
    ImagePopover,
  },
  props: {
    searchResults: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      imageLoaded: {},
      showPopover: false,
      selectedImage: null,
      searchTimestamp: 0,
    };
  },
  computed: {
    results() {
      return this.searchResults.results || [];
    },
  },
  watch: {
    "searchResults.timestamp": {
      handler(newVal) {
        this.searchTimestamp = newVal;
        this.imageLoaded = {};
      },
      immediate: true,
    },
  },
  methods: {
    showImage(image) {
      this.selectedImage = image;
      this.showPopover = true;
    },
    closePopover() {
      this.showPopover = false;
    },
  },
};
</script>

<style scoped>
.container {
  margin-top: 1rem;
  margin-bottom: 5rem;
}

.gallery_container {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  margin: 0 auto;
}

.gallery_card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(234, 94, 19, 0.2);
  border-radius: 0.5rem;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.gallery_card:hover {
  transform: translateY(-0.5rem);
  box-shadow: 0 0.5rem 1.25rem rgba(234, 94, 19, 0.2);
}

.card_url {
  color: #000;
}

.custom_link {
  color: #000;
  text-decoration: none;
}

.card_content {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

.image_container {
  position: relative;
  width: 100%;
  height: 500px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.05);
  cursor: pointer;
  flex-shrink: 0;
}

.card_image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.card_image.image-loaded {
  opacity: 1;
}

.gallery_card:hover .card_image {
  transform: scale(1.05);
}

.text_container {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  position: relative;
}

.card_title {
  color: var(--primary-color);
  margin: 0 0 1rem 0rem;
  font-size: 2rem;
  font-weight: bold;
}

.card_description {
  color: #000000;
  margin: 0;
  line-height: 1.75;
  flex-grow: 1;
  font-size: 1rem;
}

.card_date {
  color: rgba(255, 255, 255, 0.7);
  font-size: 1rem;
  margin-top: 1rem;
  align-self: flex-end;
}

.image_loader {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.05) 25%,
    rgba(234, 94, 19, 0.1) 50%,
    rgba(255, 255, 255, 0.05) 75%
  );
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 992px) {
  .gallery_grid {
    grid-template-columns: 1fr;
  }

  .image_container {
    height: auto;
  }
}
</style>
