$('a[data-toggle="tab"]').on('show.bs.tab', function (e) {
    var h = e.currentTarget.hash
    var url = "/coursera/category/" + h.replace(/#/, "")
    $(h + "").html("Loading...")
    $.getJSON(url, function (cdata) {
        $.getJSON("/coursera/topics/" + cdata.id, function (topics) {
            var items = []
            for (var i = 0; i < topics.length; i++) {
                c = topics[i]
                items.push("<p class='course' >Course topic <b>#" + c.id + "</b>, <i>" +
                    c.name + "</i>, <a id='tid_" + c.id +
                    "' href='#' class='btn seemore'>see more &raquo</a></p>")
            }
            if (items.length == 0)items.push("No courses found")
            $("" + h).html(items.join(""))
            $(h + " p.course a.seemore").each(function (i, v) {
                $(v).click(function () {
                    doModal(this)
                })
            })
        })
    })
})


function doModal(tid) {
    var id = $(tid).attr("id").split("_")[1]
    var load = "Loading..."
    $("#myModal .modal-title").html(load)
    $("#myModal .modal-body .description").html("Loading description..")
    $("#myModal .modal-body .courseList").html(load)
    $("#myModal .modal-body .comments").html(load)
    $("#myModal #topicId").val(id)
    $("#myModal").modal("show")

    $.getJSON("/coursera/topic/" + id, function (topic) {
        $("#myModal .modal-title").html(topic.name)
        $("#myModal .modal-body .description").html(topic.description)

        $.getJSON("/coursera/course/" + topic.id, function (courses) {
            var items = []
            for (var i = 0; i < courses.length; i++) {
                c = courses[i]
                items.push("<p><i>" + c.name + "</i>, Start date: " + c.startDate + "</p>")
            }
            $("#myModal .modal-body .courseList").html(items.join(""))
            updateComments()

        })


    })
}

function updateComments() {
    var id = $("#myModal #topicId").val()
    $.getJSON("/comments/topic/" + id, function (comments) {
        var items = []
        for (var i = 0; i < comments.length; i++) {
            c = comments[i]
            items.push("<p><i>" + c.content + "</i><br/>" + formatTime(c.createdAt) + "</p>")
        }
        if (items.length == 0)items.push("No comments.")
        $("#myModal .modal-body .comments").html(items.join(""))
    })
}

$("#myModal #sendbutton").click(function () {

        $.ajax({
            type: "POST",
            url: "/comments/",
            data: {
                "content": $("#myModal #content").val(),
                "topicId": $("#myModal #topicId").val()
            },
            success: function (data, text, jq) {
//                console.log(data)
                if (data === true) {
                    updateComments()
                } else {
                    $("#myModal .alertholder").html(
                        "<span class='alert alert-warning'>" + data.errors.content[0] + "</span>"
                    )
                }
            },
            dataType: "json"
        })
    }
)
var formatTime = function (unixTimestamp) {
    var dt = new Date(unixTimestamp);

    var hours = dt.getHours();
    var minutes = dt.getMinutes();
    var seconds = dt.getSeconds();

    // the above dt.get...() functions return a single digit
    // so I prepend the zero here when needed
    if (hours < 10)
        hours = '0' + hours;

    if (minutes < 10)
        minutes = '0' + minutes;

    if (seconds < 10)
        seconds = '0' + seconds;

    return dt.getDay() + "/" + dt.getMonth() + "/" + dt.getYear() + " " + hours + ":" + minutes + ":" + seconds;
}
