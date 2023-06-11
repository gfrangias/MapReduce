<?php
?>
<!DOCTYPE html>
<html>
<head>
    <title>Timeline</title>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
    <style>
        .stage {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background-color: #ddd;
            color: black;
            line-height: 50px;
            text-align: center;
            display: inline-block;
            position: relative;
            margin-right: 60px;
        }

        .stage.past {
            background-color: #28a745;
            color: white;
        }

        .stage.current {
            background-color: #007bff;
            color: white;
        }

        .stage:hover {
            cursor: pointer;
        }

        .stage:not(:last-child)::after {
            content: "";
            position: absolute;
            z-index: -1;
            right: -60px;
            top: 25px;
            height: 1px;
            width: 60px;
            background: #ddd;
        }

        .stage.past:not(:last-child)::after {
            background: #28a745;
        }

        .stage.current:not(:last-child)::after,
        .stage.current ~ .stage:not(:last-child)::after {
            border: 1px dashed #ddd;
        }

        .tooltip-inner {
            border-radius: 5px;
        }
    </style>
</head>
<body>
<div class="container mt-5">
    <div class="timeline text-center">
        <?php foreach($stages as $stage): ?>
            <div class="stage" id="<?php echo str_replace(' ', '_', $stage); ?>" data-toggle="tooltip" data-placement="bottom" title="">
                <p class="my-auto"><?php echo substr($stage, 0, 1); ?></p>
            </div>
        <?php endforeach; ?>
    </div>
</div>
    <script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"></script>
<script>
$(document).ready(function(){
    function fetchStage() {
        $.ajax({
            url: 'fetch_stage.php',
            type: 'get',
            dataType: 'json',
            success: function(response) {
                $('.stage').removeClass('current past');
                $('#'+response.stage.replace(' ', '_')).addClass('current').prevAll().addClass('past');
                $.each(response.numOfWorkers, function(stage, num) {
                    $('#'+stage.replace(' ', '_')).attr('data-original-title', stage + ': Num Of Workers ' + num);
                });
            },
            complete: function() {
                setTimeout(fetchStage, 5000);
            }
        });
    }
    fetchStage();
    $('[data-toggle="tooltip"]').tooltip();
});
</script>
</body>
</html>