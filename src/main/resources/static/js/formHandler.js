$(document).ready(function() {
    $('#comparisonForm').on('submit', function(event) {
        event.preventDefault();

        const city = $('#comparisonCity').val();
        const typeReport = $('#comparisonTypeReport').val();

        $.ajax({
            url: '/excel/av/comparison',
            method: 'POST',
            data: { city: city, typeReport: typeReport },
            success: function(response) {
                // Обновляем заголовки
                let taskNosHeader = '';
                response.taskNos.forEach(taskNo => {
                    taskNosHeader += `<th>${taskNo}</th>`;
                });
                $('#taskNosHeader').html(taskNosHeader);

                // Обновляем тело таблицы
                let reportSummaryBody = '';
                $.each(response.reportSummaryMap, function(retailChain, reports) {
                    reportSummaryBody += `<tr><td>${retailChain}</td>`;
                    response.taskNos.forEach(taskNo => {
                        let report = reports.find(r => r.taskNo === taskNo);
                        if (report) {
                            reportSummaryBody += `<td>
                                ${report.countRows}, ${report.countCompetitorsPrice}, ${report.countPromotionalPrice}
                            </td>`;
                        } else {
                            reportSummaryBody += `<td></td>`;
                        }
                    });
                    reportSummaryBody += `</tr>`;
                });
                $('#reportSummaryBody').html(reportSummaryBody);
            },
            error: function() {
                alert('Ошибка при обработке данных');
            }
        });
    });
});
