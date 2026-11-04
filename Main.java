
package com.fashionshow;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    // =====================================================
    // FIELD AND TABLE CONFIGURATION
    // =====================================================

    static class Field {
        String column;
        String label;
        String type;
        boolean required;
        boolean hidden;

        Field(String column, String label, String type, boolean required) {
            this(column, label, type, required, false);
        }

        Field(String column, String label, String type,
              boolean required, boolean hidden) {
            this.column = column;
            this.label = label;
            this.type = type;
            this.required = required;
            this.hidden = hidden;
        }
    }

    static class TableConfig {
        String path;
        String table;
        String idColumn;
        String title;
        List<Field> fields;

        TableConfig(String path, String table, String idColumn,
                    String title, Field... fields) {
            this.path = path;
            this.table = table;
            this.idColumn = idColumn;
            this.title = title;
            this.fields = Arrays.asList(fields);
        }
    }

    // =====================================================
    // MODELS
    // =====================================================

    private static final TableConfig MODELS = new TableConfig(
        "models", "MODELS", "MODEL_ID", "Models",
        new Field("NAME", "Model Name", "text", true),
        new Field("GENDER", "Gender", "gender", true),
        new Field("CONTACT", "Contact (10 digits)", "tel", true),
        new Field("HEIGHT_CM", "Height in cm", "number", true),
        new Field("EXPERIENCE_YEARS", "Experience in years", "integer", true)
    );

    // =====================================================
    // DESIGNERS
    // =====================================================

    private static final TableConfig DESIGNERS = new TableConfig(
        "designers", "DESIGNERS", "DESIGNER_ID", "Designers",
        new Field("NAME", "Designer Name", "text", true),
        new Field("BRAND_NAME", "Brand Name", "text", true),
        new Field("CONTACT", "Contact (10 digits)", "tel", true),
        new Field("EMAIL", "Email Address", "email", true)
    );

    // =====================================================
    // FASHION SHOWS
    // =====================================================

    private static final TableConfig SHOWS = new TableConfig(
        "shows", "FASHION_SHOWS", "SHOW_ID", "Fashion Shows",
        new Field("TITLE", "Show Title", "text", true),
        new Field("SHOW_DATE", "Show Date", "date", true),
        new Field("VENUE", "Venue", "text", true),
        new Field("THEME", "Theme", "text", true),

        // Hidden from website forms and table.
        // The application calculates this value automatically.
        new Field("STATUS", "Show Status", "showstatus", false, true)
    );

    // =====================================================
    // OUTFITS
    // =====================================================

    private static final TableConfig OUTFITS = new TableConfig(
    "outfits", "OUTFITS", "OUTFIT_ID", "Outfits",
    new Field("OUTFIT_NAME", "Outfit Name", "text", true),
    new Field("CATEGORY", "Category", "category", true),
    new Field("MATERIAL", "Material", "text", true),
    new Field("OUTFIT_SIZE", "Size", "size", true),
    new Field("COLOUR", "Colour", "colour", true),
    new Field("AVAILABILITY_STATUS", "Availability", "availability", true),
    new Field("DESIGNER_ID", "Designer ID", "designerid", true)
);

    private static final List<TableConfig> TABLES =
        List.of(MODELS, DESIGNERS, SHOWS, OUTFITS);

    // =====================================================
    // DROPDOWN VALUES
    // =====================================================

    private static final List<String> GENDERS =
        List.of("Male", "Female", "Other");

    private static final List<String> CATEGORIES =
        List.of(
            "Traditional Wear",
            "Western Wear",
            "Casual Wear",
            "Formal Wear",
            "Evening Wear",
            "Party Wear",
            "Ethnic Wear",
            "Other"
        );

    private static final List<String> COLOURS =
        List.of(
            "Black", "White", "Red", "Blue", "Green", "Pink",
            "Purple", "Yellow", "Orange", "Brown", "Grey", "Gray",
            "Silver", "Gold", "Beige", "Maroon", "Navy Blue",
            "Cream", "Ivory", "Teal", "Turquoise", "Coral",
            "Lavender", "Peach", "Burgundy", "Multicolour"
        );

    private static final List<String> SIZES =
        List.of("XS", "S", "M", "L", "XL", "XXL", "Free Size");

    private static final List<String> SHOW_STATUSES =
        List.of("Upcoming", "Ongoing", "Completed", "Cancelled");

    private static final List<String> AVAILABILITY_STATUSES =
        List.of("Available", "Unavailable", "Reserved");

    // =====================================================
    // ORIGINAL CSS DESIGN
    // =====================================================

    private static final String STYLE = """
        <style>
        * { box-sizing: border-box; }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #fff5fa;
            color: #333;
        }

        header {
            background: #731b4b;
            color: white;
            text-align: center;
            padding: 25px 12px;
        }

        nav {
            background: #4a1230;
            text-align: center;
            padding: 15px;
        }

        nav a {
            color: white;
            text-decoration: none;
            margin: 8px 12px;
            font-weight: bold;
        }

        nav a:hover { color: #ffc4df; }

        main {
            max-width: 1250px;
            margin: 30px auto;
            padding: 0 18px;
        }

        h2 { color: #731b4b; }

        .cards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
        }

        .card, .formbox {
            background: white;
            padding: 22px;
            border-radius: 10px;
            box-shadow: 0 4px 15px #00000012;
        }

        .card { border-top: 4px solid #a83270; }

        .button, button {
            display: inline-block;
            background: #8e245c;
            color: white;
            border: 0;
            border-radius: 5px;
            padding: 10px 15px;
            text-decoration: none;
            cursor: pointer;
            font-size: 14px;
        }

        .button:hover, button:hover { background: #541336; }
        .danger { background: #b42335; }

        .table-wrap {
            overflow-x: auto;
            background: white;
            margin-top: 20px;
            border-radius: 8px;
        }

        table {
            border-collapse: collapse;
            width: 100%;
            min-width: 750px;
        }

        th, td {
            border: 1px solid #ead5e0;
            padding: 10px;
            text-align: left;
        }

        th { background: #731b4b; color: white; }
        tr:nth-child(even) { background: #fff0f7; }

        label {
            display: block;
            margin-top: 14px;
            font-weight: bold;
        }

        input, select {
            display: block;
            width: 100%;
            padding: 10px;
            margin-top: 6px;
            border: 1px solid #cbb5c1;
            border-radius: 5px;
            background: white;
        }

        .message {
            background: white;
            padding: 14px;
            border-left: 5px solid #8e245c;
            margin: 15px 0;
            overflow-wrap: anywhere;
        }

        .actions {
            display: flex;
            gap: 7px;
            align-items: center;
        }

        footer {
            background: #4a1230;
            color: white;
            text-align: center;
            padding: 20px;
            margin-top: 45px;
        }

        .status-done {
            color: #247443;
            font-weight: bold;
        }

        .status-today {
            color: #9a6500;
            font-weight: bold;
        }

        .status-upcoming {
            color: #2459a9;
            font-weight: bold;
        }

        @media(max-width: 600px) {
            nav a { display: inline-block; }
        }
        </style>
        """;

    // =====================================================
    // MAIN METHOD
    // =====================================================

    public static void main(String[] args) {
        Javalin app = Javalin.create();

        app.get("/", ctx -> ctx.html(homePage()));

        for (TableConfig config : TABLES) {
            registerCrudRoutes(app, config);
        
        }
        app.get("/shows/participants/{showId}", Main::listShowParticipants);
app.get("/shows/participants/{showId}/add", ctx ->
    showParticipantForm(
        ctx,
        null,
        parseId(ctx.pathParam("showId")),
        "",
        null
    )
);
app.post("/shows/participants/{showId}/add", Main::saveParticipant);
app.get("/shows/participants/{showId}/edit/{id}", Main::editParticipantForm);
app.post("/shows/participants/{showId}/edit/{id}", Main::updateParticipant);
app.post("/shows/participants/{showId}/delete/{id}", Main::deleteParticipant);

        app.start(8081);

        System.out.println("Fashion Show Management System is running!");
        System.out.println("Open http://localhost:8081");
    }

    // =====================================================
    // CRUD ROUTES
    // =====================================================

    private static void registerCrudRoutes(
            Javalin app, TableConfig config) {

        // READ
        app.get("/" + config.path, ctx -> listRecords(ctx, config));

        // CREATE FORM
        app.get("/" + config.path + "/add", ctx ->
            showForm(ctx, config, null, "", null)
        );

        // CREATE
        app.post("/" + config.path + "/add", ctx ->
            saveRecord(ctx, config, null)
        );

        // EDIT FORM
        app.get("/" + config.path + "/edit/{id}", ctx -> {
            int id;

            try {
                id = parseId(ctx.pathParam("id"));
            } catch (IllegalArgumentException e) {
                showError(ctx, 400, "Invalid record ID.");
                return;
            }

            Map<String, String> record = findRecord(config, id);

            if (record == null) {
                showError(ctx, 404, "Record not found.");
                return;
            }

            showForm(ctx, config, id, "", record);
        });

        // UPDATE
        app.post("/" + config.path + "/edit/{id}", ctx -> {
            int id;

            try {
                id = parseId(ctx.pathParam("id"));
            } catch (IllegalArgumentException e) {
                showError(ctx, 400, "Invalid record ID.");
                return;
            }

            saveRecord(ctx, config, id);
        });

        // DELETE CONFIRMATION
        app.get("/" + config.path + "/delete/{id}", ctx -> {
            int id;

            try {
                id = parseId(ctx.pathParam("id"));
            } catch (IllegalArgumentException e) {
                showError(ctx, 400, "Invalid record ID.");
                return;
            }

            Map<String, String> record = findRecord(config, id);

            if (record == null) {
                showError(ctx, 404, "Record not found.");
                return;
            }

            String content =
                "<h2>Delete " + esc(config.title) + " Record</h2>" +
                "<div class='message'>Are you sure you want to delete " +
                "record ID " + id + "? This action cannot be undone.</div>" +
                "<form method='post' action='/" + config.path +
                "/delete/" + id + "'>" +
                "<button class='danger' type='submit'>Confirm Delete</button> " +
                "<a class='button' href='/" + config.path + "'>Cancel</a>" +
                "</form>";

            ctx.html(page("Confirm Delete", content));
        });

        // DELETE
        app.post("/" + config.path + "/delete/{id}", ctx -> {
            int id;

            try {
                id = parseId(ctx.pathParam("id"));
            } catch (IllegalArgumentException e) {
                showError(ctx, 400, "Invalid record ID.");
                return;
            }

            String sql = "DELETE FROM " + config.table +
                         " WHERE " + config.idColumn + " = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);

                int rows = ps.executeUpdate();

                if (rows == 0) {
                    showError(ctx, 404, "Record not found.");
                    return;
                }

                ctx.redirect("/" + config.path);

            } catch (SQLException e) {
                System.err.println("Delete failed: " + e.getMessage());

                ctx.status(409);
                ctx.html(page("Delete Failed",
                    "<div class='message'>" +
                    "This record could not be deleted. It may be referenced " +
                    "by another record. Delete or reassign dependent records first." +
                    "<p><a class='button' href='/" + config.path +
                    "'>Back</a></p></div>"));
            }
        });
    }

    // =====================================================
    // HOME PAGE
    // =====================================================

    private static String homePage() {
        StringBuilder html = new StringBuilder();

        html.append("""
            <section style="text-align:center;padding:35px;background:#f8d9e8;border-radius:12px">
                <h2>Where Fashion Meets Excellence</h2>
                <p>Manage models, designers, fashion shows and outfits.</p>
            </section>
            <div class="cards" style="margin-top:25px">
            """);

        for (TableConfig config : TABLES) {
            html.append("<div class='card'><h2>")
                .append(esc(config.title))
                .append("</h2><p>Add, view, update and delete records.</p>")
                .append("<a class='button' href='/")
                .append(config.path)
                .append("'>Manage ")
                .append(esc(config.title))
                .append("</a></div>");
        }

        html.append("</div>");

        return page("Home", html.toString());
    }

    // =====================================================
    // AUTOMATIC SHOW STATUS
    // =====================================================

    private static String calculateStoredShowStatus(String dateValue) {
        LocalDate showDate = LocalDate.parse(dateValue);
        LocalDate today = LocalDate.now();

        if (showDate.isBefore(today)) {
            return "Completed";
        } else if (showDate.isEqual(today)) {
            return "Ongoing";
        }

        return "Upcoming";
    }

    private static String getDateStatus(java.sql.Date showDate) {
        if (showDate == null) {
            return "Date not set";
        }

        LocalDate today = LocalDate.now();
        LocalDate date = showDate.toLocalDate();

        if (date.isBefore(today)) {
            return "Done";
        } else if (date.isEqual(today)) {
            return "Today";
        }

        return "Upcoming";
    }

    private static String getDateStatusClass(String status) {
        return switch (status) {
            case "Done" -> "status-done";
            case "Today" -> "status-today";
            case "Upcoming" -> "status-upcoming";
            default -> "";
        };
    }

    // =====================================================
    // READ ALL RECORDS
    // =====================================================

    private static void listRecords(Context ctx, TableConfig config) {
        String sql = config == OUTFITS
    ? "SELECT * FROM OUTFITS ORDER BY OUTFIT_ID"
    : "SELECT * FROM " + config.table +
      " ORDER BY " + config.idColumn;

        StringBuilder html = new StringBuilder();

        html.append("<div style='display:flex;gap:12px;flex-wrap:wrap;")
            .append("justify-content:space-between;align-items:center'>")
            .append("<h2>").append(esc(config.title)).append("</h2>")
            .append("<div><a class='button' href='/'>Home</a> ")
            .append("<a class='button' href='/").append(config.path)
            .append("/add'>+ Add New</a></div></div>");

        html.append("<div class='table-wrap'><table><thead><tr><th>")
            .append(esc(config.idColumn))
            .append("</th>");

        for (Field field : config.fields) {
            if (field.hidden) {
                continue;
            }

            html.append("<th>")
                .append(esc(field.label))
                .append("</th>");
        }

        // Display calculated Date Status instead of stored Show Status.
        if (config == SHOWS) {
            html.append("<th>Date Status</th>");
        }

        html.append("<th>Actions</th></tr></thead><tbody>");

        int count = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                count++;

                int id = rs.getInt(config.idColumn);

                html.append("<tr><td>")
                    .append(id)
                    .append("</td>");

                for (Field field : config.fields) {
                    if (field.hidden) {
                        continue;
                    }

                    Object value = rs.getObject(field.column);
String displayValue;


if (field.type.equals("date")) {

    java.sql.Date date = rs.getDate(field.column);

    displayValue = date == null
        ? "-"
        : date.toLocalDate().toString();

} else {

    displayValue = value == null
        ? "-"
        : value.toString();
}
                    html.append("<td>")
                        .append(esc(displayValue))
                        .append("</td>");
                }

                if (config == SHOWS) {
                    java.sql.Date showDate = rs.getDate("SHOW_DATE");
                    String dateStatus = getDateStatus(showDate);

                    html.append("<td><span class='")
                        .append(getDateStatusClass(dateStatus))
                        .append("'>")
                        .append(esc(dateStatus))
                        .append("</span></td>");
                }

                html.append("<td><div class='actions'>")
                    .append("<a class='button' href='/")
                    .append(config.path)
                    .append("/edit/")
                    .append(id)
                    .append("'>Edit</a>")
                    .append("<a class='button danger' href='/")
.append(config.path)
.append("/delete/")
.append(id)
.append("'>Delete</a>");

if (config == SHOWS) {
    html.append("<a class='button' href='/shows/participants/")
        .append(id)
        .append("'>Manage Participants</a>");
}

html.append("</div></td></tr>");
            }

        } catch (SQLException e) {
            System.err.println("Read failed: " + e.getMessage());

            ctx.status(500);
            ctx.html(page("Database Error",
                "<div class='message'>Could not read records. Check the " +
                "Oracle connection, table names and column names.</div>"));

            return;
        }

        if (count == 0) {
            int visibleFields = (int) config.fields.stream()
                .filter(f -> !f.hidden).count();

            html.append("<tr><td colspan='")
                .append(visibleFields + (config == SHOWS ? 3 : 2))
                .append("'>No records found.</td></tr>");
        }

        html.append("</tbody></table></div>")
            .append("<p>Total records: <strong>")
            .append(count)
            .append("</strong></p>");

        ctx.html(page(config.title, html.toString()));
    }
    // =====================================================
// SHOW PARTICIPANTS - READ
// =====================================================

private static void listShowParticipants(Context ctx) {
    int showId = parseId(ctx.pathParam("showId"));

    String sql = """
        SELECT p.PARTICIPANT_ID,
               p.MODEL_ID,
               m.NAME AS MODEL_NAME,
               p.DESIGNER_ID,
               d.NAME AS DESIGNER_NAME
        FROM SHOW_PARTICIPANTS p
        JOIN MODELS m ON p.MODEL_ID = m.MODEL_ID
        JOIN DESIGNERS d ON p.DESIGNER_ID = d.DESIGNER_ID
        WHERE p.SHOW_ID = ?
        ORDER BY p.PARTICIPANT_ID
        """;

    StringBuilder html = new StringBuilder();

    html.append("<h2>Show Participants</h2>")
        .append("<p>Fashion Show ID: ").append(showId).append("</p>")
        .append("<a class='button' href='/shows'>Back to Shows</a> ")
        .append("<a class='button' href='/shows/participants/")
        .append(showId).append("/add'>+ Add Participant</a>")
        .append("<div class='table-wrap'><table>")
        .append("<thead><tr>")
        .append("<th>Model ID</th>")
        .append("<th>Model Name</th>")
        .append("<th>Designer ID</th>")
        .append("<th>Designer Name</th>")
        .append("<th>Actions</th>")
        .append("</tr></thead><tbody>");

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, showId);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("PARTICIPANT_ID");

                html.append("<tr><td>")
                    .append(rs.getInt("MODEL_ID"))
                    .append("</td><td>")
                    .append(esc(rs.getString("MODEL_NAME")))
                    .append("</td><td>")
                    .append(rs.getInt("DESIGNER_ID"))
                    .append("</td><td>")
                    .append(esc(rs.getString("DESIGNER_NAME")))
                    .append("</td><td><div class='actions'>")
                    .append("<a class='button' href='/shows/participants/")
                    .append(showId).append("/edit/").append(id)
                    .append("'>Edit</a>")
                    .append("<form method='post' action='/shows/participants/")
                    .append(showId).append("/delete/").append(id)
                    .append("'>")
                    .append("<button class='danger' type='submit'>Delete</button>")
                    .append("</form></div></td></tr>");
            }
        }

    } catch (SQLException e) {
        System.err.println("Participant read failed: " + e.getMessage());
        showError(ctx, 500, "Could not load show participants.");
        return;
    }

    html.append("</tbody></table></div>");
    ctx.html(page("Show Participants", html.toString()));
}



// =====================================================
// SHOW PARTICIPANT FORM
// =====================================================

private static void showParticipantForm(
        Context ctx, Integer participantId, int showId,
        String error, Map<String, String> values) {

    boolean editing = participantId != null;

    String modelValue = values == null ? "" :
        values.getOrDefault("MODEL_ID", "");

    String designerValue = values == null ? "" :
        values.getOrDefault("DESIGNER_ID", "");

    StringBuilder html = new StringBuilder();

    html.append("<h2>")
        .append(editing ? "Edit" : "Add")
        .append(" Participant</h2>");

    if (error != null && !error.isBlank()) {
        html.append("<div class='message'>")
            .append(esc(error)).append("</div>");
    }

    html.append("<div class='formbox' style='max-width:650px'>")
        .append("<form method='post' action='/shows/participants/")
        .append(showId)
        .append(editing ? "/edit/" + participantId : "/add")
        .append("'>");

    html.append("<label>Model ID *</label>")
        .append("<select name='modelid' required>");

    addOption(html, "", "Select Model ID", modelValue);

    for (String modelId : getIdOptions("MODELS", "MODEL_ID")) {
        addOption(html, modelId, modelId, modelValue);
    }

    html.append("</select>");

    html.append("<label>Designer ID *</label>")
        .append("<select name='designerid' required>");

    addOption(html, "", "Select Designer ID", designerValue);

    for (String designerId : getIdOptions("DESIGNERS", "DESIGNER_ID")) {
        addOption(html, designerId, designerId, designerValue);
    }

    html.append("</select>")
        .append("<p><button type='submit'>")
        .append(editing ? "Update Participant" : "Save Participant")
        .append("</button> ")
        .append("<a class='button' href='/shows/participants/")
        .append(showId).append("'>Cancel</a></p>")
        .append("</form></div>");

    ctx.html(page("Participant Form", html.toString()));
}


// =====================================================
// ADD PARTICIPANT
// =====================================================

private static void saveParticipant(Context ctx) {
    int showId = parseId(ctx.pathParam("showId"));

    String modelId = ctx.formParam("modelid");
    String designerId = ctx.formParam("designerid");

    Map<String, String> values = new LinkedHashMap<>();
    values.put("MODEL_ID", modelId == null ? "" : modelId.trim());
    values.put("DESIGNER_ID", designerId == null ? "" : designerId.trim());

    if (!validForeignKey("FASHION_SHOWS", "SHOW_ID",
            String.valueOf(showId)) ||
        !validForeignKey("MODELS", "MODEL_ID", values.get("MODEL_ID")) ||
        !validForeignKey("DESIGNERS", "DESIGNER_ID", values.get("DESIGNER_ID"))) {

        ctx.status(400);
        showParticipantForm(ctx, null, showId,
            "Select valid Model ID and Designer ID values.", values);
        return;
    }

    String sql = """
        INSERT INTO SHOW_PARTICIPANTS
        (PARTICIPANT_ID, SHOW_ID, MODEL_ID, DESIGNER_ID)
        VALUES (
            (SELECT NVL(MAX(PARTICIPANT_ID), 0) + 1
             FROM SHOW_PARTICIPANTS), ?, ?, ?)
        """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, showId);
        ps.setInt(2, Integer.parseInt(values.get("MODEL_ID")));
        ps.setInt(3, Integer.parseInt(values.get("DESIGNER_ID")));

        ps.executeUpdate();
        ctx.redirect("/shows/participants/" + showId);

    } catch (SQLException e) {
        System.err.println("Participant save failed: " + e.getMessage());
        ctx.status(500);
        showParticipantForm(ctx, null, showId,
            "Could not save participant. Check whether this association already exists.",
            values);
    }
}


// =====================================================
// EDIT PARTICIPANT FORM
// =====================================================

private static void editParticipantForm(Context ctx) {
    int showId = parseId(ctx.pathParam("showId"));
    int id = parseId(ctx.pathParam("id"));

    String sql = """
        SELECT MODEL_ID, DESIGNER_ID
        FROM SHOW_PARTICIPANTS
        WHERE PARTICIPANT_ID = ? AND SHOW_ID = ?
        """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);
        ps.setInt(2, showId);

        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                showError(ctx, 404, "Participant not found.");
                return;
            }

            Map<String, String> values = new LinkedHashMap<>();
            values.put("MODEL_ID", String.valueOf(rs.getInt("MODEL_ID")));
            values.put("DESIGNER_ID", String.valueOf(rs.getInt("DESIGNER_ID")));

            showParticipantForm(ctx, id, showId, "", values);
        }

    } catch (SQLException e) {
        showError(ctx, 500, "Could not load participant.");
    }
}


// =====================================================
// UPDATE PARTICIPANT
// =====================================================

private static void updateParticipant(Context ctx) {
    int showId = parseId(ctx.pathParam("showId"));
    int id = parseId(ctx.pathParam("id"));

    String modelId = ctx.formParam("modelid");
    String designerId = ctx.formParam("designerid");

    if (!validForeignKey("MODELS", "MODEL_ID", modelId) ||
        !validForeignKey("DESIGNERS", "DESIGNER_ID", designerId)) {
        showError(ctx, 400, "Select valid Model ID and Designer ID values.");
        return;
    }

    String sql = """
        UPDATE SHOW_PARTICIPANTS
        SET MODEL_ID = ?, DESIGNER_ID = ?
        WHERE PARTICIPANT_ID = ? AND SHOW_ID = ?
        """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, Integer.parseInt(modelId));
        ps.setInt(2, Integer.parseInt(designerId));
        ps.setInt(3, id);
        ps.setInt(4, showId);

        if (ps.executeUpdate() == 0) {
            showError(ctx, 404, "Participant not found.");
            return;
        }

        ctx.redirect("/shows/participants/" + showId);

    } catch (SQLException e) {
        showError(ctx, 500, "Could not update participant.");
    }
}


// =====================================================
// DELETE PARTICIPANT
// =====================================================

private static void deleteParticipant(Context ctx) {
    int showId = parseId(ctx.pathParam("showId"));
    int id = parseId(ctx.pathParam("id"));

    String sql = """
        DELETE FROM SHOW_PARTICIPANTS
        WHERE PARTICIPANT_ID = ? AND SHOW_ID = ?
        """;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);
        ps.setInt(2, showId);
        ps.executeUpdate();

        ctx.redirect("/shows/participants/" + showId);

    } catch (SQLException e) {
        showError(ctx, 500, "Could not delete participant.");
    }
}


// =====================================================
// GET MODEL / DESIGNER IDS
// =====================================================

private static List<String> getIdOptions(String table, String column) {
    List<String> ids = new java.util.ArrayList<>();

    String sql = "SELECT " + column + " FROM " + table +
                 " ORDER BY " + column;

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            ids.add(String.valueOf(rs.getInt(1)));
        }

    } catch (SQLException e) {
        System.err.println("Could not load IDs: " + e.getMessage());
    }

    return ids;
}

    // =====================================================
    // CREATE AND UPDATE
    // =====================================================

    private static void saveRecord(
            Context ctx,
            TableConfig config,
            Integer id) {

        Map<String, String> values = new LinkedHashMap<>();

        for (Field field : config.fields) {
            if (field.hidden) {
                continue;
            }

            String value = ctx.formParam(field.column.toLowerCase());

            values.put(
                field.column,
                value == null ? "" : value.trim()
            );
        }

        String error = validate(config, values);

        if (!error.isEmpty()) {
            ctx.status(400);
            showForm(ctx, config, id, error, values);
            return;
        }

        // Automatically generate STATUS from SHOW_DATE.
        if (config == SHOWS) {
            values.put(
                "STATUS",
                calculateStoredShowStatus(values.get("SHOW_DATE"))
            );
        }

        try {
            if (duplicateExists(config, values, id)) {
                ctx.status(409);

                showForm(
                    ctx,
                    config,
                    id,
                    "Duplicate record! A record with the same details already exists.",
                    values
                );

                return;
            }

        } catch (SQLException e) {
            System.err.println("Duplicate check failed: " + e.getMessage());

            ctx.status(500);

            showForm(
                ctx,
                config,
                id,
                "Could not check for duplicate records. Please try again.",
                values
            );

            return;
        }

        boolean updating = id != null;
        String sql;

        if (updating) {
            StringBuilder setClause = new StringBuilder();

            for (Field field : config.fields) {
                if (setClause.length() > 0) {
                    setClause.append(", ");
                }

                setClause.append(field.column).append(" = ?");
            }

            sql = "UPDATE " + config.table +
                  " SET " + setClause +
                  " WHERE " + config.idColumn + " = ?";

        } else {
            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();

            for (Field field : config.fields) {
                if (columns.length() > 0) {
                    columns.append(", ");
                    placeholders.append(", ");
                }

                columns.append(field.column);
                placeholders.append("?");
            }

            sql = "INSERT INTO " + config.table +
                  " (" + config.idColumn + ", " + columns + ") " +
                  "VALUES ((SELECT NVL(MAX(" + config.idColumn +
                  "), 0) + 1 FROM " + config.table + "), " +
                  placeholders + ")";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int index = 1;

            // Bind all database fields, including hidden STATUS.
            for (Field field : config.fields) {
                String value = values.get(field.column);

                switch (field.type) {
                    case "date" ->
                        ps.setDate(
                            index++,
                            java.sql.Date.valueOf(value)
                        );

                    case "number" ->
                        ps.setBigDecimal(
                            index++,
                            new BigDecimal(value)
                        );

                    case "integer", "designerid", "showid", "modelid" ->
    ps.setInt(index++, Integer.parseInt(value));
                    default ->
                        ps.setString(index++, value);
                }
            }

            if (updating) {
                ps.setInt(index, id);
            }

            int rows = ps.executeUpdate();

            if (updating && rows == 0) {
                showError(ctx, 404, "Record not found.");
                return;
            }

            ctx.redirect("/" + config.path);

        } catch (SQLException e) {
            System.err.println("Save failed: " + e.getMessage());

            ctx.status(500);

            String message =
                "Oracle could not save this record. Check the database " +
                "columns and constraints. See the VS Code terminal for details.";

            if (e.getErrorCode() == 1) {
                message = "A duplicate value violates a database unique constraint.";
            } else if (e.getErrorCode() == 2291) {
                message = "The selected Designer ID or Fashion Show ID does not exist.";
            }

            showForm(ctx, config, id, message, values);

        } catch (IllegalArgumentException e) {
            ctx.status(400);

            showForm(
                ctx,
                config,
                id,
                "One or more field values are invalid.",
                values
            );
        }
    }

    // =====================================================
    // DUPLICATE DETECTION
    // =====================================================

    private static boolean duplicateExists(
            TableConfig config,
            Map<String, String> values,
            Integer currentId) throws SQLException {

        // Hidden STATUS must not affect duplicate detection.
        List<Field> duplicateFields = config.fields.stream()
            .filter(field -> !field.hidden)
            .toList();

        StringBuilder sql = new StringBuilder(
            "SELECT 1 FROM " + config.table + " WHERE "
        );

        for (int i = 0; i < duplicateFields.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }

            Field field = duplicateFields.get(i);

            if (List.of(
                "text", "gender", "tel", "email",
                "showstatus", "category", "size",
                "availability", "colour"
            ).contains(field.type)) {

                sql.append("LOWER(TRIM(")
                   .append(field.column)
                   .append(")) = LOWER(TRIM(?))");

            } else {
                sql.append(field.column).append(" = ?");
            }
        }

        if (currentId != null) {
            sql.append(" AND ")
               .append(config.idColumn)
               .append(" <> ?");
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int index = 1;

            for (Field field : duplicateFields) {
                String value = values.get(field.column);

                switch (field.type) {
                    case "date" ->
                        ps.setDate(
                            index++,
                            java.sql.Date.valueOf(value)
                        );

                    case "number" ->
                        ps.setBigDecimal(
                            index++,
                            new BigDecimal(value)
                        );

                    case "integer", "designerid", "showid", "modelid" ->
    ps.setInt(index++, Integer.parseInt(value));

                    default ->
                        ps.setString(index++, value);
                }
            }

            if (currentId != null) {
                ps.setInt(index, currentId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // =====================================================
    // SERVER-SIDE VALIDATION
    // =====================================================

    private static String validate(
            TableConfig config,
            Map<String, String> values) {

        for (Field field : config.fields) {
            if (field.hidden) {
                continue;
            }

            String value = values.getOrDefault(field.column, "").trim();

            if (field.required && value.isEmpty()) {
                return field.label + " is required.";
            }

            if (value.isEmpty()) {
                continue;
            }

            switch (field.type) {
                case "text" -> {
                    if (value.length() < 2 || value.length() > 100) {
                        return field.label + " must be 2 to 100 characters.";
                    }

                    if (!value.matches(
                        "[\\p{L}\\p{N}][\\p{L}\\p{N} .,'&()/-]{1,99}")) {
                        return field.label + " contains invalid characters.";
                    }
                }

                case "gender" -> {
                    if (!GENDERS.contains(value)) {
                        return "Select a valid gender.";
                    }
                }

                case "tel" -> {
                    if (!value.matches("[0-9]{10}")) {
                        return "Contact must contain exactly 10 digits.";
                    }
                }

                
case "email" -> {
    if (value.length() > 254 ||
        !value.matches(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@" +
            "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?" +
            "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*$")) {
        return "Enter a valid email address.";
    }

    String domain = value.substring(value.lastIndexOf('@') + 1);

    List<String> allowedDomains = List.of(
        "example.com",
        "gmail.com",
        "yahoo.com"
    );

    if (!allowedDomains.contains(domain)) {
        return "Only @example.com, @gmail.com, or @yahoo.com are allowed (case-sensitive).";
    }
}


                case "number" -> {
                    try {
                        BigDecimal number = new BigDecimal(value);

                        if (number.compareTo(new BigDecimal("100")) < 0 ||
                            number.compareTo(new BigDecimal("250")) > 0 ||
                            number.scale() > 2) {

                            return "Height must be 100-250 cm, with at most 2 decimal places.";
                        }

                    } catch (NumberFormatException e) {
                        return "Enter a valid height.";
                    }
                }

                case "integer" -> {
                    try {
                        if (!value.matches("[0-9]+")) {
                            return "Experience must be a whole number.";
                        }

                        int number = Integer.parseInt(value);

                        if (number > 80) {
                            return "Experience must be between 0 and 80 years.";
                        }

                    } catch (NumberFormatException e) {
                        return "Enter a valid experience value.";
                    }
                }

                case "date" -> {
                    try {
                        LocalDate.parse(value);
                    } catch (Exception e) {
                        return "Enter a valid date.";
                    }
                }

                case "showstatus" -> {
                    if (!SHOW_STATUSES.contains(value)) {
                        return "Select a valid fashion show status.";
                    }
                }

                case "category" -> {
                    if (!CATEGORIES.contains(value)) {
                        return "Select a valid outfit category.";
                    }
                }

                case "colour" -> {
                    if (!COLOURS.contains(value)) {
                        return "Select a valid colour from the dropdown.";
                    }
                }

                case "size" -> {
                    if (!SIZES.contains(value)) {
                        return "Select a valid outfit size.";
                    }
                }

                case "availability" -> {
                    if (!AVAILABILITY_STATUSES.contains(value)) {
                        return "Select a valid availability status.";
                    }
                }

                case "designerid" -> {
                    if (!validForeignKey(
                        "DESIGNERS", "DESIGNER_ID", value)) {
                        return "Designer ID does not exist. Add the designer first.";
                    }
                }

                case "showid" -> {
                    if (!validForeignKey(
                        "FASHION_SHOWS", "SHOW_ID", value)) {
                        return "Fashion Show ID does not exist. Add the show first.";
                    }
                }
                case "modelid" -> {
    if (!validForeignKey("MODELS", "MODEL_ID", value)) {
        return "Model ID does not exist. Add the model first.";
    }
}
            }
        }

        return "";
    }

    // =====================================================
    // FOREIGN KEY VALIDATION
    // =====================================================

    private static boolean validForeignKey(
            String table,
            String column,
            String value) {

        try {
            int id = Integer.parseInt(value);

            if (id <= 0) {
                return false;
            }

            String sql = "SELECT 1 FROM " + table +
                         " WHERE " + column + " = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }

        } catch (SQLException | NumberFormatException e) {
            return false;
        }
    }

    // =====================================================
    // CREATE AND UPDATE FORMS
    // =====================================================

    private static void showForm(
            Context ctx,
            TableConfig config,
            Integer id,
            String error,
            Map<String, String> values) {

        boolean editing = id != null;

        StringBuilder html = new StringBuilder();

        html.append("<h2>")
            .append(editing ? "Update " : "Add ")
            .append(esc(config.title))
            .append("</h2>");

        if (error != null && !error.isBlank()) {
            html.append("<div class='message' role='alert' ")
                .append("style='border-left-color:#c62828'>")
                .append(esc(error))
                .append("</div>");
        }

        html.append("<div class='formbox' style='max-width:650px'>")
            .append("<form method='post' action='/")
            .append(config.path)
            .append(editing ? "/edit/" + id : "/add")
            .append("'>");

        for (Field field : config.fields) {
            // Never show hidden fields in forms.
            if (field.hidden) {
                continue;
            }

            String key = field.column.toLowerCase();

            String value = values == null
                ? ""
                : values.getOrDefault(field.column, "");

            html.append("<label for='")
                .append(key)
                .append("'>")
                .append(esc(field.label))
                .append(field.required ? " *" : "")
                .append("</label>");

            List<String> options = optionsFor(field.type);

if (field.type.equals("modelid")) {

    Map<String, String> models = getModelOptions();

    html.append("<select id='")
        .append(key)
        .append("' name='")
        .append(key)
        .append("' required>");

    addOption(html, "", "Select a model", value);

    for (Map.Entry<String, String> model : models.entrySet()) {
        addOption(
            html,
            model.getKey(),
            model.getKey(),
            value
        );
    }

    html.append("</select>");

} else if (options != null) {
                html.append("<select id='")
                    .append(key)
                    .append("' name='")
                    .append(key)
                    .append("'")
                    .append(field.required ? " required" : "")
                    .append(">");

                addOption(html, "", "Select " + field.label, value);

                for (String option : options) {
                    addOption(html, option, option, value);
                }

                html.append("</select>");

            } else {
                String inputType = "text";
                String extra = "";

                switch (field.type) {
                    case "email" -> {
                        inputType = "email";
                        extra = " maxlength='254'";
                    }

                    case "tel" -> {
                        inputType = "tel";
                        extra = " maxlength='10' pattern='[0-9]{10}'";
                    }

                    case "number" -> {
                        inputType = "number";
                        extra = " min='100' max='250' step='0.01'";
                    }

                    case "integer" -> {
                        inputType = "number";
                        extra = " min='0' max='80' step='1'";
                    }

                    case "date" -> inputType = "date";

                    case "designerid", "showid" -> {
                        inputType = "number";
                        extra = " min='1' step='1'";
                    }
                }

                html.append("<input id='")
                    .append(key)
                    .append("' name='")
                    .append(key)
                    .append("' type='")
                    .append(inputType)
                    .append("'")
                    .append(extra)
                    .append(" maxlength='")
                    .append(field.type.equals("email") ? 254 : 100)
                    .append("'")
                    .append(field.required ? " required" : "")
                    .append(" value='")
                    .append(esc(value))
                    .append("'>");

                if (field.type.equals("designerid") ||
                    field.type.equals("showid")) {

                    html.append("<small>Enter an existing ")
                        .append(
                            field.type.equals("designerid")
                                ? "Designer ID"
                                : "Fashion Show ID"
                        )
                        .append(" from its listing page.</small>");
                }
            }
        }

        html.append("<p style='margin-top:22px'>")
            .append("<button type='submit'>")
            .append(editing ? "Update Record" : "Save Record")
            .append("</button> ")
            .append("<a class='button' href='/")
            .append(config.path)
            .append("'>Cancel</a></p></form></div>");

        ctx.html(page(
            editing ? "Update " + config.title : "Add " + config.title,
            html.toString()
        ));
    }
    private static Map<String, String> getModelOptions() {

    Map<String, String> models = new LinkedHashMap<>();

    String sql =
    "SELECT MODEL_ID FROM MODELS ORDER BY MODEL_ID";

    try (
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()
    ) {

        while (rs.next()) {
            models.put(
    String.valueOf(rs.getInt("MODEL_ID")),
    String.valueOf(rs.getInt("MODEL_ID"))
);
        }

    } catch (SQLException e) {
        System.err.println(
            "Could not load models: " + e.getMessage()
        );
    }

    return models;
}

    private static List<String> optionsFor(String type) {
        return switch (type) {
            case "gender" -> GENDERS;
            case "category" -> CATEGORIES;
            case "colour" -> COLOURS;
            case "size" -> SIZES;
            case "showstatus" -> SHOW_STATUSES;
            case "availability" -> AVAILABILITY_STATUSES;
            default -> null;
        };
    }

    private static void addOption(
            StringBuilder html,
            String value,
            String label,
            String selected) {

        html.append("<option value='")
            .append(esc(value))
            .append("'")
            .append(value.equals(selected) ? " selected" : "")
            .append(">")
            .append(esc(label))
            .append("</option>");
    }

    // =====================================================
    // FIND ONE RECORD
    // =====================================================

    private static Map<String, String> findRecord(
            TableConfig config,
            int id) {

        String sql = "SELECT * FROM " + config.table +
                     " WHERE " + config.idColumn + " = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Map<String, String> record = new LinkedHashMap<>();

                for (Field field : config.fields) {
                    Object value = rs.getObject(field.column);

                    if (field.type.equals("date")) {
                        java.sql.Date date = rs.getDate(field.column);

                        record.put(
                            field.column,
                            date == null ? "" : date.toLocalDate().toString()
                        );

                    } else {
                        record.put(
                            field.column,
                            value == null ? "" : value.toString()
                        );
                    }
                }

                return record;
            }

        } catch (SQLException e) {
            System.err.println("Find record failed: " + e.getMessage());
            return null;
        }
    }

    // =====================================================
    // ERROR PAGE
    // =====================================================

    private static void showError(
            Context ctx,
            int status,
            String message) {

        ctx.status(status);

        ctx.html(page(
            "Error",
            "<div class='message'>" + esc(message) +
            "<p><a class='button' href='/'>Home</a></p></div>"
        ));
    }

    // =====================================================
    // COMMON HTML PAGE
    // =====================================================

    private static String page(String title, String content) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>""" + esc(title) + " | Fashion Show Management</title>" + STYLE + """
            </head>
            <body>
                <header>
                    <h1>Fashion Show Management System</h1>
                    <p>Models | Designers | Fashion Shows | Outfits</p>
                </header>

                <nav>
                    <a href="/">Home</a>
                    <a href="/models">Models</a>
                    <a href="/designers">Designers</a>
                    <a href="/shows">Fashion Shows</a>
                    <a href="/outfits">Outfits</a>
                </nav>

                <main>
            """ + content + """
                </main>

                <footer>Fashion Show Management System </footer>
            </body>
            </html>
            """;
    }

    // =====================================================
    // VALIDATE RECORD ID
    // =====================================================

    private static int parseId(String value) {
        try {
            int id = Integer.parseInt(value);

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid record ID.");
        }
    }

    // =====================================================
    // ESCAPE HTML
    // =====================================================

    private static String esc(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
