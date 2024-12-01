document.addEventListener("DOMContentLoaded", function() {
    loadSelectedValue("comparisonCity");
    loadSelectedValue("comparisonTypeReport");

    document.getElementById("comparisonCity").addEventListener("change", function() {
        saveSelectedValue("comparisonCity");
    });

    document.getElementById("comparisonTypeReport").addEventListener("change", function() {
        saveSelectedValue("comparisonTypeReport");
    });
});

function saveSelectedValue(selectId) {
    const selectElement = document.getElementById(selectId);
    localStorage.setItem(selectId, selectElement.value);
}

function loadSelectedValue(selectId) {
    const selectedValue = localStorage.getItem(selectId);
    if (selectedValue) {
        const selectElement = document.getElementById(selectId);
        selectElement.value = selectedValue;
    }
}
