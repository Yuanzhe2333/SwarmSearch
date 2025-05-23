<template>
  <div class="pagination-bar-container">
    <div class="row">
      <div class="search-method">
        <span class="medium-text">Search method</span>
        <Select
          checkmark
          class="custom-selector align-vertical-center"
          v-model="selectedSearchMethod"
          :options="searchMethodOptions"
          placeholder="Select search method"
          :highlightOnSelect="false"
          @update:modelValue="handleSearchMethodChange"
          size="large"
          v-tooltip.bottom="'Change the search method'"
        />
      </div>

      <Transition name="fade">
        <div
          class="tokenizer-option"
          v-if="selectedSearchMethod === 'Regular search'"
        >
          <span class="medium-text">Tokenizer</span>
          <Select
            checkmark
            class="custom-selector align-vertical-center"
            v-model="selectedTokenizer"
            :options="tokenizerOptions"
            placeholder="Select a tokenizer"
            :highlightOnSelect="false"
            @update:modelValue="handleTokenizerChange"
            size="large"
            v-tooltip.bottom="'Change the tokenizer'"
          />
        </div>
      </Transition>

      <Transition name="fade">
        <div class="page-size">
          <span class="medium-text">Items per page</span>
          <Select
            checkmark
            class="custom-selector align-vertical-center"
            v-model="selectedPageSize"
            :options="pageSizes"
            placeholder="selectedPageSize"
            :highlightOnSelect="false"
            :modelValue="selectedPageSize"
            @update:modelValue="handlePageSizeChange"
            size="large"
            v-tooltip.bottom="'Change the number of items per page'"
          />
        </div>
      </Transition>
    </div>
  </div>

  <div class="row pagination-buttons">
    <button
      :disabled="currentPage === 1"
      @click="goToFirstPage"
      class="pagination-btn medium-text"
      v-tooltip.bottom="'Go to the first page'"
    >
      <i class="fas fa-angle-double-left"></i>
      <span class="button-text">First</span>
    </button>
    <button
      :disabled="currentPage === 1"
      @click="goToPreviousPage"
      class="pagination-btn medium-text"
      v-tooltip.bottom="'Go to the previous page'"
    >
      <i class="fas fa-chevron-left"></i>
      <span class="button-text">Previous</span>
    </button>

    <span class="current-page medium-text">
      {{ currentPage }} / {{ maxPages }}
    </span>

    <button
      :disabled="currentPage === maxPages"
      @click="goToNextPage"
      v-tooltip.bottom="'Go to the next page'"
      class="pagination-btn medium-text"
    >
      <i class="fas fa-chevron-right"></i> <span class="button-text">Next</span>
    </button>
    <button
      :disabled="currentPage === maxPages"
      @click="goToLastPage"
      v-tooltip.bottom="'Go to the last page'"
      class="pagination-btn medium-text"
    >
      <i class="fas fa-angle-double-right"></i>
      <span class="button-text">Last</span>
    </button>
  </div>
</template>

<script>
import Select from "primevue/select";

export default {
  name: "PaginationBar",
  components: {
    Select,
  },
  props: {
    currentPage: {
      type: Number,
      required: true,
    },
    maxPages: {
      type: Number,
      required: true,
    },
    yearOptions: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      selectedPageSize: 10,
      pageSizes: [10, 20, 50],
      selectedYear: null,
      selectedSearchMethod: "Regular search",
      searchMethodOptions: ["Regular search", "Semantic search"],
      selectedTokenizer: "Standard",
      tokenizerOptions: ["Standard", "N-Gram"],
    };
  },
  methods: {
    handlePageSizeChange() {
      this.$emit("page-size-change", this.selectedPageSize);
    },
    handleYearChange() {
      this.$emit("year-change", this.selectedYear);
    },
    goToFirstPage() {
      this.$emit("go-to-page", 1);
    },
    goToPreviousPage() {
      if (this.currentPage > 1) {
        this.$emit("go-to-page", this.currentPage - 1);
      }
    },
    goToNextPage() {
      if (this.currentPage < this.maxPages) {
        this.$emit("go-to-page", this.currentPage + 1);
      }
    },
    goToLastPage() {
      this.$emit("go-to-page", this.maxPages);
    },
    handleSearchMethodChange() {
      this.$emit("search-method-change", this.selectedSearchMethod);
    },
    handleTokenizerChange() {
      this.$emit("tokenizer-change", this.selectedTokenizer);
      this.selectedYear = null;
    },
  },
};
</script>

<style scoped>
.pagination-bar-container {
  width: 100%;
  border: 1px solid rgba(234, 94, 19, 0.2);
  background: rgba(255, 255, 255, 0.05);
  color: #000;
  padding: 1.5rem;
  margin-top: 2rem;
}

.pagination-bar-container .row {
  display: flex;
  gap: 1.25rem;
  flex-wrap: wrap;
  justify-content: space-between;
  color: #000;
}

.custom-selector {
  border-radius: 0.5rem;
  height: 4rem;
  background: transparent;
  color: #000;
  width: 100%;
}

.pagination-buttons {
  color: #000;
  display: flex;
  align-items: center;
  justify-content: end;
  width: 100%;
  gap: 0.5rem;
  margin-top: 5rem;
}

.pagination-btn {
  color: #000;
  background: transparent;
  height: 4rem;
  border: 1px solid rgba(234, 94, 19, 0.2);
  padding: 0 0.8rem;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.pagination-btn:disabled {
  cursor: not-allowed;
  color: #666;
}

.pagination-btn:hover {
  background-color: rgba(234, 94, 19, 0.1);
}

.current-page {
  margin: 0 1rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.year-filter,
.search-method,
.tokenizer-option,
.page-size {
  display: flex;
  flex-direction: column;
  align-items: start;
  gap: 0.5rem;
  flex-grow: 1;
  color: #000;
}

@media screen and (max-width: 992px) {
  .custom-selector {
    height: 3rem;
  }

  .pagination-btn {
    height: 3rem;
  }

  .pagination-buttons {
    margin-top: 3rem;
  }
}

@media screen and (max-width: 768px) {
  .pagination-buttons {
    margin-top: 2rem;
  }
}

@media screen and (max-width: 576px) {
  .pagination-btn .button-text {
    display: none;
  }

  .pagination-buttons {
    margin-top: 1rem;
  }
}
</style>
