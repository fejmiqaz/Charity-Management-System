// =========================================================
// Charity Organization – shared front-end behaviour
// =========================================================

document.addEventListener("DOMContentLoaded", function () {
  initDeleteConfirmation();
  initFadeIn();
});

/**
 * Every delete <form> has a trigger button with class "co-delete-trigger"
 * (instead of submitting straight away). Clicking it opens the shared
 * confirmation modal (#coConfirmDeleteModal); confirming submits the
 * original form, so every existing th:action target keeps working
 * completely unchanged.
 *
 * Optional: put a data-item-label="..." on the trigger button to show
 * the item's name in the confirmation text.
 */
function initDeleteConfirmation() {
  const modalEl = document.getElementById("coConfirmDeleteModal");
  if (!modalEl || typeof bootstrap === "undefined") return;

  const modal = new bootstrap.Modal(modalEl);
  const confirmBtn = document.getElementById("coConfirmDeleteBtn");
  const modalText = document.getElementById("coConfirmDeleteText");
  let pendingForm = null;

  document.querySelectorAll(".co-delete-trigger").forEach(function (btn) {
    btn.addEventListener("click", function (e) {
      e.preventDefault();
      pendingForm = btn.closest("form");
      const label = btn.getAttribute("data-item-label");
      modalText.textContent = label
        ? 'A jeni i sigurt që doni të fshini "' + label + '"?'
        : "A jeni i sigurt që doni ta fshini këtë element?";
      modal.show();
    });
  });

  confirmBtn.addEventListener("click", function () {
    if (pendingForm) {
      pendingForm.submit();
    }
    modal.hide();
  });
}

/** Small entrance animation for cards / tables as they load. */
function initFadeIn() {
  document.querySelectorAll(".card, .table-branded").forEach(function (el) {
    el.classList.add("fade-in");
  });
}
